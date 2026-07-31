import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater( () ->
        {
            InventoryGUI gui = new InventoryGUI();
            gui.setVisible(true);
        }
        );
    }
}
