
#include <jni.h>
/* Header for class uk_ac_ucl_umobile_net_UbiCDNService */

#ifndef _Included_uk_ac_ucl_umobile_net_UbiCDNService
#define _Included_uk_ac_ucl_umobile_net_UbiCDNService
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     uk_ac_ucl_umobile_net_UbiCDNService
 * Method:    startNfd
 * Signature: (Ljava/lang/Map;)V
 */
JNIEXPORT void JNICALL
Java_uk_ac_ucl_kbapp_KebappService_startNfd(JNIEnv*, jclass, jobject);

/*
 * Class:     uk_ac_ucl_umobile_net_UbiCDNService
 * Method:    stopNfd
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_uk_ac_ucl_kbapp_KebappService_stopNfd(JNIEnv*, jclass);

/*
 * Class:     uk_ac_ucl_umobile_net_UbiCDNService
 * Method:    isNfdRunning
 * Signature: ()L/java/lang/Boolean;
 */
JNIEXPORT jboolean JNICALL
Java_uk_ac_ucl_kbapp_KebappService_isNfdRunning(JNIEnv*, jclass);

/*
 * Class:     uk_ac_ucl_umobile_net_UbiCDNService
 * Method:    getNfdLogModules
 * Signature: ()Ljava/util/List;
 */
JNIEXPORT jobject JNICALL
Java_uk_ac_ucl_kbapp_KebappService_getNfdLogModules(JNIEnv*, jclass);

#ifdef __cplusplus
}
#endif
#endif

