#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_edu_mit_ll_hadr_psiapanalytictester_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
