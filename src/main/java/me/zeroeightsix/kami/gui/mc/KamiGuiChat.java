package me.zeroeightsix.kami.gui.mc;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

public class KamiGuiChat extends GuiChat {

    private String startString;
    private String currentFillinLine;
    private int cursor;

    public KamiGuiChat(String startString, String historybuffer, int sentHistoryCursor) {
        super(startString);
        this.startString = startString;
        if (!startString.equals(Command.getCommandPrefix()))
            calculateCommand(startString.substring(Command.getCommandPrefix().length()));
        this.historyBuffer = historybuffer;
        cursor = sentHistoryCursor;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.sentHistoryCursor = cursor;
        super.keyTyped(typedChar, keyCode);
        cursor = this.sentHistoryCursor;

        String chatLine = this.inputField.getText();

        if (!chatLine.startsWith(Command.getCommandPrefix())){
            GuiChat newGUI = new GuiChat(chatLine) {
                int cursor = KamiGuiChat.this.cursor;
                @Override
                protected void keyTyped(char typedChar, int keyCode) throws IOException {
                    this.sentHistoryCursor = cursor;
                    super.keyTyped(typedChar, keyCode);
                    cursor = this.sentHistoryCursor;
                }
            };
            newGUI.historyBuffer = this.historyBuffer;
            mc.displayGuiScreen(newGUI);
            return;
        }

        // Startstring is still here! Hooray!

        if (chatLine.equals(Command.getCommandPrefix())) {
            currentFillinLine = "";
            return;
        }

        calculateCommand(chatLine.substring(Command.getCommandPrefix().length()));
    }

    protected void calculateCommand(String line){
        String[] args = line.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        HashMap<String, Command> options = new HashMap<String, Command>();

        if (args.length == 0) return; // Hell naw!

        for (Command c : KamiMod.getInstance().getCommandManager().getCommands()) {
            if ((c.getLabel().startsWith(args[0]) && !line.endsWith(" ")) || c.getLabel().equals(args[0])) {
                options.put(c.getLabel(), c);
            }
        }

        if (options.isEmpty()) {
            currentFillinLine = "";
            return;
        }

        TreeMap<String, Command> map = new TreeMap<String, Command>(options);

        Command alphaCommand = map.firstEntry().getValue();

        currentFillinLine = alphaCommand.getLabel().substring(args[0].length());

        if (alphaCommand.getSyntaxChunks() == null || alphaCommand.getSyntaxChunks().length == 0)
            return;

        if (!line.endsWith(" "))
            currentFillinLine += " ";

        SyntaxChunk[] chunks = alphaCommand.getSyntaxChunks();

        boolean cutSpace = false;
        for(int i = 0; i < chunks.length; i++){
            if (i+1 < args.length-1) continue;
            SyntaxChunk c = chunks[i];

            String result = c.getChunk(chunks, c, args, (i+1==args.length-1 ? args[i+1] : null));
            if (result != "" && (!result.startsWith("<") || !result.endsWith(">")) && (!result.startsWith("[") || !result.endsWith("]")))
                cutSpace = true;
            currentFillinLine += result + (result == "" ? "" : " ") + "";
        }

        if (cutSpace)
            currentFillinLine = currentFillinLine.substring(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);

        int x = this.inputField.fontRenderer.getStringWidth(this.inputField.getText() + "")+4;
        int y = this.inputField.getEnableBackgroundDrawing() ? this.inputField.y + (this.inputField.height - 8) / 2 : this.inputField.y;
        this.inputField.fontRenderer.drawStringWithShadow(currentFillinLine, x, y, 0x666666);

        this.inputField.drawTextBox();
        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null)
        {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
        }

        /*int x = this.inputField.fontRendererInstance.getStringWidth(this.inputField.getText() + "")+4;
        int y = this.inputField.getEnableBackgroundDrawing() ? this.inputField.yPosition + (this.inputField.height - 8) / 2 : this.inputField.yPosition;
        this.inputField.fontRendererInstance.drawString(currentFillinLine, x, y, 0x666666);*/

        boolean a = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean b = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(0.8f, 0.1f, 0f);
        GL11.glBegin(GL11.GL_LINES); {
            GL11.glVertex2f(this.inputField.x-2, this.inputField.y-2);
            GL11.glVertex2f(this.inputField.x+this.inputField.getWidth()-2, this.inputField.y-2);

            GL11.glVertex2f(this.inputField.x+this.inputField.getWidth()-2, this.inputField.y-2);
            GL11.glVertex2f(this.inputField.x+this.inputField.getWidth()-2, this.inputField.y+this.inputField.height-2);

            GL11.glVertex2f(this.inputField.x+this.inputField.getWidth()-2, this.inputField.y+this.inputField.height-2);
            GL11.glVertex2f(this.inputField.x-2, this.inputField.y+this.inputField.height-2);

            GL11.glVertex2f(this.inputField.x-2, this.inputField.y+this.inputField.height-2);
            GL11.glVertex2f(this.inputField.x-2, this.inputField.y-2);
        }
        GL11.glEnd();

        if(a)
            GL11.glEnable(GL11.GL_BLEND);
        if(b)
            GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
