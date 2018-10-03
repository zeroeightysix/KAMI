package me.zeroeightsix.kami.gui.old.rgui.render.util;

import static org.lwjgl.opengl.ARBShaderObjects.*;
import static org.lwjgl.opengl.GL11.GL_FALSE;

/**
 * Some utils for ARB Shaders
 *
 * @author Brady
 * @since 2/16/2017 12:00 PM
 */
public final class ShaderHelper {

    private ShaderHelper() {}

    /**
     * Loads a shader program from its program ID
     *
     * @param programID The ARB Shader program ID
     */
    public static void createProgram(int programID) {
        glLinkProgramARB(programID);
        checkObjecti(programID, GL_OBJECT_LINK_STATUS_ARB);
        glValidateProgramARB(programID);
        checkObjecti(programID, GL_OBJECT_VALIDATE_STATUS_ARB);
    }

    /**
     * Loads a shader of the specified type from the specified path
     *
     * @param path Shader path
     * @param type Shader type
     * @return The Shader's Object ID
     */
    public static int loadShader(String path, int type) {
        int shaderID = glCreateShaderObjectARB(type);
        if (shaderID == 0)
            return 0;

        String src = new StreamReader(ShaderHelper.class.getResourceAsStream(path)).read();
        glShaderSourceARB(shaderID, src);
        glCompileShaderARB(shaderID);
        checkObjecti(shaderID, GL_OBJECT_COMPILE_STATUS_ARB);
        return shaderID;
    }

    /**
     * Gets the error that an object produced
     *
     * @param objID The object's ID
     * @return The error
     */
    private static String getLogInfo(int objID) {
        return glGetInfoLogARB(objID, glGetObjectParameteriARB(objID, GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    /**
     * Checks an arb object parameter
     *
     * @param objID The object's ID
     * @param name The name of the object
     */
    private static void checkObjecti(int objID, int name) {
        if (glGetObjectParameteriARB(objID, name) == GL_FALSE)
            try {
                throw new Exception(getLogInfo(objID));
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}