package me.zeroeightsix.kami.gui.old.rgui.render.util;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.ARBShaderObjects.*;

/**
 * A representation of a GLSL Uniform Variable
 *
 * @author Brady
 * @since 2/16/2017 12:00 PM
 */
public final class Uniform {

    /**
     * The Uniform name
     */
    private final String name;

    /**
     * The Uniform Object ID
     */
    private final int location;

    private Uniform(String name, int location) {
        this.name = name;
        this.location = location;
    }

    /**
     * Sets the value of this Uniform as an Int
     *
     * @param value New value
     */
    public final void setInt(int value) {
        glUniform1iARB(location, value);
    }

    /**
     * Sets the value of this Uniform as a Float
     *
     * @param value New value
     */
    public final void setFloat(float value) {
        glUniform1fARB(location, value);
    }

    /**
     * Sets the value of this Uniform as a Boolean
     *
     * @param value New value
     */
    public final void setBoolean(boolean value) {
        glUniform1fARB(location, value ? 1 : 0);
    }

    /**
     * Sets the value of this Uniform as a Vec2
     *
     * @param value New value
     */
    public final void setVec(Vector2f value) {
        glUniform2fARB(location, value.x, value.y);
    }

    /**
     * Sets the value of this Uniform as a Vec3d
     *
     * @param value New value
     */
    public final void setVec(Vector3f value) {
        glUniform3fARB(location, value.x, value.y, value.z);
    }

    /**
     * @return The name of this UniformVariable
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @return The Object ID of this UniformVariable
     */
    public final int getLocation() {
        return this.location;
    }

    /**
     * Creates a uniform variable from the shader object id and the uniform's name
     *
     * @param shaderID Shader object ID
     * @param uniformName Uniform Name
     * @return The UniformVariable representation
     */
    public static Uniform get(int shaderID, String uniformName) {
        return new Uniform(uniformName, glGetUniformLocationARB(shaderID, uniformName));
    }
}