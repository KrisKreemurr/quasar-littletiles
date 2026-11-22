package team.creative.littletiles.common.gui.control;

import team.creative.creativecore.common.gui.IGuiParent;

import java.util.ArrayList;
import java.util.List;


import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiControl;

public class GuiActionDisplay extends GuiControl {
    
    public List<ActionMessage> messages = new ArrayList<>();
    
    private int maxActions;
    public int displayTime;
    public int fadeTime;
    public int totalTime;
    
    public GuiActionDisplay(IGuiParent parent, String name) {
        super(parent, name);
        setTimer(5, 1);
        setMessageCount(6);
    }
    
    public GuiActionDisplay setMessageCount(int count) {
        this.maxActions = count;
        List<ActionMessage> newMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (i >= count)
                break;
            newMessages.add(messages.get(i));
        }
        this.messages = newMessages;
        return this;
    }
    
    public GuiActionDisplay setTimer(int displaySeconds, int fadeSeconds) {
        this.displayTime = displaySeconds * 1000;
        this.fadeTime = fadeSeconds * 1000;
        this.totalTime = displayTime + fadeTime;
        return this;
    }
    
    public void clearMessages() {
        messages.clear();
        reflow();
    }
    
    public void addMessage(List<Component> message) {
        messages.add(0, new ActionMessage(message));
        if (messages.size() > maxActions)
            for (int i = maxActions; i < messages.size(); i++)
                messages.remove(i);
        reflow();
    }
    
    public void removeMessage(int index) {
        messages.remove(index);
        reflow();
    }
    
    @Override
    public void closed() {}
    
    @Override
    public void init() {}
    
    @Override
    public void tick() {}
    
    /** Plain data so the class stays loadable on a dedicated server; the client compiles and caches the text. */
    public static class ActionMessage {
        
        public final List<Component> text;
        public final long timestamp;
        public Object clientCache;
        
        public ActionMessage(List<Component> message) {
            this.text = message;
            this.timestamp = System.currentTimeMillis();
        }
        
    }
    
}
