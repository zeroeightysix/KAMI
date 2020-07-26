package me.zeroeightsix.kami

import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Field
import kotlin.reflect.KMutableProperty

object KotlinTypeMagic {
    @JvmStatic
    fun getKotlinAnnotatedType(pojo: Any, setting: Field): AnnotatedType {
        val annotations = (pojo::class.members.find {
            it.name == setting.name && it is KMutableProperty
        } ?: return setting.annotatedType).returnType.annotations.toMutableList()

        val type = setting.annotatedType
        annotations.addAll(type.annotations)

        val arrayOfAnnotations = annotations.toTypedArray()

        return when (type) {
            is AnnotatedArrayType -> {
                object : AnnotatedArrayType by type {
                    override fun getAnnotations(): Array<Annotation> = arrayOfAnnotations
                }
            }
            is AnnotatedParameterizedType -> {
                object : AnnotatedParameterizedType by type {
                    override fun getAnnotations(): Array<Annotation> = arrayOfAnnotations
                }
            }
            else -> {
                object : AnnotatedType by type {
                    override fun getAnnotations(): Array<Annotation> = arrayOfAnnotations
                }
            }
        }
    }
}
