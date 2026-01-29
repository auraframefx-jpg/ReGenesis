#include <jni.h>
#include <string>
#include <sched.h>
#include <android/log.h>
#include "bitnet.h"

#define TAG "BitNetJNI"

// Static instance for the session
static BitNetModel* model = nullptr;

extern "C" /**
 * Generate a model response for the given prompt using a shared BitNetModel instance.
 *
 * Lazily initializes a process-global BitNetModel on first use (model path set to
 * "/sdcard/models/bitnet-100b.gguf") and attempts to pin the calling thread to CPU
 * cores 4–7 to optimize performance. Converts the provided Java string prompt to UTF-8,
 * invokes the model to produce a response, and returns that response as a newly created
 * Java UTF string.
 *
 * @param j_prompt Java UTF-8 string containing the input prompt.
 * @return jstring A new Java UTF string containing the generated response, or `nullptr`
 *         if the input string conversion fails or an error occurs.
 */
JNIEXPORT jstring JNICALL
Java_dev_aurakai_auraframefx_services_BitNetLocalService_generateLocalResponse(
    JNIEnv* env,
    jobject /* this */,
    jstring j_prompt) {

    // Lazy initialization of the model
    if (!model) {
        // Path would be injected or configured in a real scenario
        model = new BitNetModel("/sdcard/models/bitnet-100b.gguf");

        // Thermal/Performance Optimization: Pin to Big Cores (e.g., cores 4-7 on Snapdragon)
        cpu_set_t set;
        CPU_ZERO(&set);
        for (int i = 4; i < 8; ++i) {
            CPU_SET(i, &set);
        }

        if (sched_setaffinity(0, sizeof(set), &set) < 0) {
            __android_log_print(ANDROID_LOG_WARN, TAG, "Failed to set CPU affinity");
        } else {
            __android_log_print(ANDROID_LOG_INFO, TAG, "Pinned thread to big cores (4-7)");
        }
    }

    const char* prompt_cstr = env->GetStringUTFChars(j_prompt, nullptr);
    if (!prompt_cstr) {
        return nullptr;
    }

    std::string prompt(prompt_cstr);

    // Perform Inference
    std::string response = model->generate(prompt);

    env->ReleaseStringUTFChars(j_prompt, prompt_cstr);

    return env->NewStringUTF(response.c_str());
}