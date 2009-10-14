#include "org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl.h"
#include "native_layer.h"

/*
 * Class:     org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl
 * Method:    init_native
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl_init_1native
  (JNIEnv * env, jobject jobj) {
	return init(1);
}

/*
 * Class:     org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl
 * Method:    terminate_native
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl_terminate_1native
  (JNIEnv * env, jobject jobj) {
	return terminate();
}

/*
 * Class:     org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl
 * Method:    send_message_native
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl_send_1message_1native
	(JNIEnv * env, jobject jobj, jbyteArray joa) {
	void * data_ptr;

	int length = (int) env->GetArrayLength(joa);
	data_ptr = malloc(length);
	if (data_ptr == NULL) {
		perror("MALLOC FAILED");
	}

	jbyte * bufByte = env->GetByteArrayElements(joa, NULL);
	memcpy(data_ptr, bufByte, length);

	// sending to the native layer
	//TODO check return error
	send_message(length, data_ptr);

	free(data_ptr);
}

/*
 * Class:     org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl
 * Method:    recv_message_native
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl_recv_1message_1native
  (JNIEnv * env, jobject jobj) {
	jbyteArray ret;
	int length;
	void * data_ptr;

	// invoke native layer
	//TODO check return error
	recv_message(&length, &data_ptr);

	// convert received message to java data structure
	ret = env->NewByteArray(length);
	env->SetByteArrayRegion(ret, 0, length, (jbyte*) data_ptr);

	// todo check data has been commit
	free(data_ptr);

	return ret;
}

/*
 * Class:     org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl
 * Method:    debug_native
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_org_objectweb_proactive_extensions_nativeinterface_coupling_NativeInterfaceImpl_debug_1native
  (JNIEnv * env, jobject jo, jbyteArray jba) {


}

