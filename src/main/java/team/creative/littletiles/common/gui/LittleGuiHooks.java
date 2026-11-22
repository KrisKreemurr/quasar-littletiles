package team.creative.littletiles.common.gui;

/** Small hooks a common GUI layer can implement; the client layer dist ({@code LittleClientLayer}) calls them. */
public class LittleGuiHooks {
    
    /** Called on the client right before the layer draws. */
    public static interface PreRender {
        
        public void preRender();
        
    }
    
    /** The layer closes when the LittleTiles configure key is pressed again. */
    public static interface CloseOnConfigureKey {}
    
}
