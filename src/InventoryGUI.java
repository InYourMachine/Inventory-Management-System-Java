import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;


// Main inventory GUI with separate action windows and a persistent product table.

public class InventoryGUI extends JFrame {

    // palette
    private static final Color BG = new Color(247, 248, 250);
    private static final Color PANEL = Color.WHITE;
    private static final Color PRIMARY = new Color(74, 111, 165);
    private static final Color ACCENT = new Color(91, 138, 114);
    private static final Color DANGER = new Color(199, 91, 91);
    private static final Color TEXT = new Color(45, 52, 54);
    private static final Color MUTED = new Color(120, 130, 140);
    private static final Color BORDER = new Color(230, 233, 238);
    private static final Color TABLE_ALT = new Color(248, 250, 252);

    private final InventoryManager manager = new InventoryManager();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Price", "Qty", "Category", "Status", "Extra"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable sharedTable = createStyledTable(tableModel);

    public InventoryGUI() {
        setTitle("Inventory Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        initMainWindow();
        refreshTable();
    }

    private void initMainWindow() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        JPanel sidebar = buildSidebar();
        JPanel tablePanel = buildTablePanel("All Products");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, tablePanel);
        split.setDividerLocation(260);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(BG);

        root.add(split, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(52, 64, 78));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel title = new JLabel("Inventory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(180, 190, 200));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(0, 0, 28, 0));

        sidebar.add(title);
        sidebar.add(subtitle);

        sidebar.add(navButton("Add Product", PRIMARY, e -> new AddProductWindow(this).setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Search Products", PRIMARY, e -> new SearchWindow(this).setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Stock Operations", ACCENT, e -> new StockWindow(this).setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Update Product", PRIMARY, e -> new UpdateProductWindow(this).setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Delete Product", DANGER, e -> deleteSelectedOrPrompt()));
        sidebar.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(70, 82, 96));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));

        sidebar.add(navButton("View All", new Color(90, 100, 115), e -> {
            refreshTable();
            JOptionPane.showMessageDialog(this, "Product list refreshed.", "View All",
                    JOptionPane.INFORMATION_MESSAGE);
        }));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Low Stock Alert", DANGER, e -> showLowStockAlert()));

        sidebar.add(Box.createVerticalGlue());

        JLabel hint = new JLabel("<html><center style='color:#9aa5b1'>Select a row in the<br>table for quick actions</center></html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(hint);

        return sidebar;
    }

    private JButton navButton(String text, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(220, 42));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    JPanel buildTablePanel(String titleText) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(20, 16, 20, 20));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT);

        JLabel countLabel = new JLabel(" ");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(MUTED);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(sharedTable);
        scroll.setBorder(new LineBorder(BORDER));
        scroll.getViewport().setBackground(PANEL);
        panel.add(scroll, BorderLayout.CENTER);

        // Store count updater via client property on panel
        panel.putClientProperty("countLabel", countLabel);
        updateCountLabel(countLabel);
        return panel;
    }

    private void updateCountLabel(JLabel label) {
        if (label != null) {
            label.setText(tableModel.getRowCount() + " product(s)");
        }
    }

    void refreshTable() {
        refreshTable(null);
    }

    void refreshTable(List<Product> products) {
        tableModel.setRowCount(0);
        try {
            List<Product> list = products != null ? products : manager.viewAllProducts();
            for (Product p : list) {
                tableModel.addRow(rowFromProduct(p));
            }
        } catch (ProductException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        repaintTableCounts();
    }

    private void repaintTableCounts() {
        Component[] comps = getContentPane().getComponents();
        // Update main count if needed
        invalidate();
        repaint();
    }

    static Object[] rowFromProduct(Product p) {
        String extra = "";
        if (p instanceof GroceryProduct) {
            extra = "Exp: " + ((GroceryProduct) p).getExpiryDate();
        } else if (p instanceof ElectronicProduct) {
            extra = ((ElectronicProduct) p).getWarrantyMonths() + " mo warranty";
        }
        return new Object[]{
                p.getProductId(),
                p.getProductName(),
                String.format("$%.2f", p.getPrice()),
                p.getQuantity(),
                p.getCategory(),
                p.getStockStatus(),
                extra
        };
    }

    private Integer getSelectedProductId() {
        int row = sharedTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (Integer) tableModel.getValueAt(row, 0);
    }

    private void deleteSelectedOrPrompt() {
        Integer id = getSelectedProductId();
        String idStr = id != null ? String.valueOf(id) : JOptionPane.showInputDialog(this,
                "Enter Product ID to delete:", "Delete Product", JOptionPane.QUESTION_MESSAGE);
        if (idStr == null || idStr.trim().isEmpty()) {
            return;
        }
        try {
            int productId = Integer.parseInt(idStr.trim());
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete product ID " + productId + "?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                manager.deleteProduct(productId);
                JOptionPane.showMessageDialog(this, "Product deleted.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid product ID.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ProductException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLowStockAlert() {
        try {
            List<Product> lowStock = manager.getLowStockProducts();
            refreshTable(lowStock);
            if (lowStock.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No low stock items (all products have quantity >= 5).",
                        "Low Stock", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder msg = new StringBuilder("LOW STOCK (quantity < 5):\n\n");
                for (Product p : lowStock) {
                    msg.append("• ").append(p.getProductName())
                            .append(" (ID ").append(p.getProductId())
                            .append(") — ").append(p.getQuantity()).append(" left\n");
                }
                JOptionPane.showMessageDialog(this, msg.toString(),
                        "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
            }
        } catch (ProductException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Shared UI helpers ───────────────────────────────────────────────

    static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(220, 232, 248));
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 244, 248));
        header.setForeground(TEXT);
        header.setReorderingAllowed(false);
        header.setBorder(new LineBorder(BORDER));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focused, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                if (!selected) {
                    c.setBackground(row % 2 == 0 ? PANEL : TABLE_ALT);
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });
        return table;
    }

    static JPanel formPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(24, 28, 28, 28)));
        return p;
    }

    static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(12, 0, 4, 0));
        return lbl;
    }

    static JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)));
        return f;
    }

    static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setBackground(PANEL);
        return c;
    }

    static JButton actionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    static Product buildProduct(String idText, String name, String priceText, String qtyText,
                                String category, String extra, boolean requireId)
            throws ProductException {
        if (name == null || name.trim().isEmpty()) {
            throw new ProductException("Product name is required.");
        }
        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceText.trim());
            quantity = Integer.parseInt(qtyText.trim());
        } catch (NumberFormatException e) {
            throw new ProductException("Price and quantity must be valid numbers.");
        }
        if (price < 0 || quantity < 0) {
            throw new ProductException("Price and quantity cannot be negative.");
        }

        int id = 0;
        if (requireId) {
            try {
                id = Integer.parseInt(idText.trim());
            } catch (NumberFormatException e) {
                throw new ProductException("Valid Product ID is required.");
            }
        }

        switch (category) {
            case "Grocery":
                if (extra == null || extra.trim().isEmpty()) {
                    throw new ProductException("Expiry date is required for Grocery.");
                }
                return requireId
                        ? new GroceryProduct(id, name.trim(), price, quantity, extra.trim())
                        : new GroceryProduct(0, name.trim(), price, quantity, extra.trim());
            case "Electronic":
                int warranty;
                try {
                    warranty = Integer.parseInt(extra.trim());
                } catch (NumberFormatException e) {
                    throw new ProductException("Warranty months must be a valid integer.");
                }
                return requireId
                        ? new ElectronicProduct(id, name.trim(), price, quantity, warranty)
                        : new ElectronicProduct(0, name.trim(), price, quantity, warranty);
            default:
                return requireId
                        ? new Product(id, name.trim(), price, quantity, "General")
                        : new Product(0, name.trim(), price, quantity, "General");
        }
    }

    // Base class for action windows with form + table side by side.
    abstract static class ActionWindow extends JFrame {
        protected final InventoryGUI parent;
        protected final DefaultTableModel localModel;
        protected final JTable localTable;

        ActionWindow(InventoryGUI parent, String title, int width, int height) {
            this.parent = parent;
            setTitle(title);
            setSize(width, height);
            setLocationRelativeTo(parent);
            getContentPane().setBackground(BG);

            localModel = new DefaultTableModel(
                    new String[]{"ID", "Name", "Price", "Qty", "Category", "Status", "Extra"}, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            localTable = createStyledTable(localModel);
        }

        protected JPanel buildSplitLayout(JPanel formSide, String tableTitle) {
            JPanel tableSide = parent.buildTablePanel(tableTitle);
            // Replace shared table with local copy for this window
            JScrollPane scroll = (JScrollPane) ((BorderLayout) tableSide.getLayout())
                    .getLayoutComponent(BorderLayout.CENTER);
            scroll.setViewportView(localTable);
            syncLocalTable();

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formSide, tableSide);
            split.setDividerLocation(380);
            split.setDividerSize(6);
            split.setBorder(null);
            split.setBackground(BG);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(BG);
            root.add(split, BorderLayout.CENTER);
            return root;
        }

        void syncLocalTable() {
            localModel.setRowCount(0);
            try {
                List<Product> list = parent.manager.viewAllProducts();
                for (Product p : list) {
                    localModel.addRow(rowFromProduct(p));
                }
            } catch (ProductException ignored) {
            }
        }

        protected void onSuccess(String message) {
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            syncLocalTable();
            parent.refreshTable();
        }

        protected void onError(ProductException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        protected void onLowStockWarning(ProductException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Low Stock Alert",
                    JOptionPane.WARNING_MESSAGE);
            syncLocalTable();
            parent.refreshTable();
        }
    }

    // ─── Add Product Window ────────────────────────────────────────────────

    static class AddProductWindow extends ActionWindow {
        private final JTextField txtName = styledField(18);
        private final JTextField txtPrice = styledField(10);
        private final JTextField txtQuantity = styledField(10);
        private final JComboBox<String> cmbCategory = styledCombo(
                new String[]{"General", "Grocery", "Electronic"});
        private final JTextField txtExtra = styledField(14);
        private final JLabel lblExtra = fieldLabel("Extra field");

        AddProductWindow(InventoryGUI parent) {
            super(parent, "Add Product", 1050, 580);
            JPanel form = formPanel();

            JLabel heading = new JLabel("New Product");
            heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
            heading.setForeground(TEXT);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(heading);

            JLabel desc = new JLabel("Fill in the details and save to inventory.");
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            desc.setForeground(MUTED);
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            desc.setBorder(new EmptyBorder(0, 0, 8, 0));
            form.add(desc);

            form.add(fieldLabel("Product Name"));
            form.add(txtName);
            form.add(fieldLabel("Price ($)"));
            form.add(txtPrice);
            form.add(fieldLabel("Quantity"));
            form.add(txtQuantity);
            form.add(fieldLabel("Category"));
            form.add(cmbCategory);
            form.add(lblExtra);
            form.add(txtExtra);

            cmbCategory.addActionListener(e -> updateExtraLabel());
            updateExtraLabel();

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btnRow.setOpaque(false);
            btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnRow.setBorder(new EmptyBorder(20, 0, 0, 0));

            JButton save = actionButton("Save Product", ACCENT);
            save.addActionListener(e -> saveProduct());
            JButton clear = actionButton("Clear", new Color(160, 170, 180));
            clear.addActionListener(e -> clearForm());
            btnRow.add(save);
            btnRow.add(clear);
            form.add(btnRow);

            setContentPane(buildSplitLayout(form, "Current Inventory"));
        }

        private void updateExtraLabel() {
            String cat = (String) cmbCategory.getSelectedItem();
            if ("Grocery".equals(cat)) {
                lblExtra.setText("Expiry Date (YYYY-MM-DD)");
            } else if ("Electronic".equals(cat)) {
                lblExtra.setText("Warranty (months)");
            } else {
                lblExtra.setText("Extra (not needed for General)");
                txtExtra.setText("");
            }
        }

        private void saveProduct() {
            try {
                Product p = buildProduct("", txtName.getText(), txtPrice.getText(),
                        txtQuantity.getText(), (String) cmbCategory.getSelectedItem(),
                        txtExtra.getText(), false);
                parent.manager.addProduct(p);
                onSuccess("Product added successfully!");
                clearForm();
            } catch (ProductException ex) {
                onError(ex);
            }
        }

        private void clearForm() {
            txtName.setText("");
            txtPrice.setText("");
            txtQuantity.setText("");
            txtExtra.setText("");
            cmbCategory.setSelectedIndex(0);
        }
    }

    // ─── Search Window ─────────────────────────────────────────────────────

    static class SearchWindow extends ActionWindow {
        private final JTextField txtSearch = styledField(18);
        private final JToggleButton btnById = new JToggleButton("By ID", true);
        private final JToggleButton btnByName = new JToggleButton("By Name");

        SearchWindow(InventoryGUI parent) {
            super(parent, "Search Products", 1050, 580);
            JPanel form = formPanel();

            JLabel heading = new JLabel("Search");
            heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
            heading.setForeground(TEXT);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(heading);

            JLabel desc = new JLabel("Find products by exact ID or partial name match.");
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            desc.setForeground(MUTED);
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            desc.setBorder(new EmptyBorder(0, 0, 16, 0));
            form.add(desc);

            JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            modeRow.setOpaque(false);
            modeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            styleToggle(btnById);
            styleToggle(btnByName);
            ButtonGroup group = new ButtonGroup();
            group.add(btnById);
            group.add(btnByName);
            btnById.addActionListener(e -> btnByName.setSelected(false));
            btnByName.addActionListener(e -> btnById.setSelected(false));
            modeRow.add(btnById);
            modeRow.add(btnByName);
            form.add(modeRow);

            form.add(fieldLabel("Search term"));
            form.add(txtSearch);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btnRow.setOpaque(false);
            btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnRow.setBorder(new EmptyBorder(20, 0, 0, 0));

            JButton search = actionButton("Search", PRIMARY);
            search.addActionListener(e -> doSearch());
            JButton showAll = actionButton("Show All", new Color(160, 170, 180));
            showAll.addActionListener(e -> {
                syncLocalTable();
                parent.refreshTable();
            });
            btnRow.add(search);
            btnRow.add(showAll);
            form.add(btnRow);

            setContentPane(buildSplitLayout(form, "Search Results"));
        }

        private void styleToggle(JToggleButton btn) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setBackground(PANEL);
            btn.setBorder(new LineBorder(BORDER));
        }

        private void doSearch() {
            String term = txtSearch.getText().trim();
            if (term.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a search term.", "Search",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                localModel.setRowCount(0);
                if (btnById.isSelected()) {
                    int id = Integer.parseInt(term);
                    Product p = parent.manager.searchById(id);
                    localModel.addRow(rowFromProduct(p));
                    JOptionPane.showMessageDialog(this, "Found 1 product.", "Search",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    List<Product> list = parent.manager.searchByName(term);
                    for (Product p : list) {
                        localModel.addRow(rowFromProduct(p));
                    }
                    JOptionPane.showMessageDialog(this, "Found " + list.size() + " product(s).",
                            "Search", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID search requires a numeric ID.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ProductException ex) {
                onError(ex);
            }
        }
    }

    // ─── Stock Operations Window ───────────────────────────────────────────

    static class StockWindow extends ActionWindow {
        private final JTextField txtId = styledField(10);
        private final JTextField txtAmount = styledField(10);

        StockWindow(InventoryGUI parent) {
            super(parent, "Stock Operations", 1050, 580);
            JPanel form = formPanel();

            JLabel heading = new JLabel("Stock");
            heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
            heading.setForeground(TEXT);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(heading);

            JLabel desc = new JLabel("Restock inventory or record a sale (reduce stock).");
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            desc.setForeground(MUTED);
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            desc.setBorder(new EmptyBorder(0, 0, 16, 0));
            form.add(desc);

            form.add(fieldLabel("Product ID"));
            form.add(txtId);
            form.add(fieldLabel("Amount"));
            form.add(txtAmount);

            JLabel tip = new JLabel("<html><span style='color:#78848f'>Tip: click a row in the table to auto-fill ID</span></html>");
            tip.setAlignmentX(Component.LEFT_ALIGNMENT);
            tip.setBorder(new EmptyBorder(8, 0, 0, 0));
            form.add(tip);

            localTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && localTable.getSelectedRow() >= 0) {
                    txtId.setText(String.valueOf(localModel.getValueAt(localTable.getSelectedRow(), 0)));
                }
            });

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btnRow.setOpaque(false);
            btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnRow.setBorder(new EmptyBorder(24, 0, 0, 0));

            JButton restock = actionButton("Restock (+)", ACCENT);
            restock.addActionListener(e -> doRestock());
            JButton sell = actionButton("Sell (−)", DANGER);
            sell.addActionListener(e -> doSell());
            btnRow.add(restock);
            btnRow.add(sell);
            form.add(btnRow);

            setContentPane(buildSplitLayout(form, "Inventory"));
        }

        private void doRestock() {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                int amount = Integer.parseInt(txtAmount.getText().trim());
                parent.manager.restock(id, amount);
                onSuccess("Restocked " + amount + " unit(s) for product #" + id);
                txtAmount.setText("");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Enter valid ID and amount.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ProductException ex) {
                onError(ex);
            }
        }

        private void doSell() {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                int amount = Integer.parseInt(txtAmount.getText().trim());
                parent.manager.reduceStock(id, amount);
                onSuccess("Sold " + amount + " unit(s) from product #" + id);
                txtAmount.setText("");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Enter valid ID and amount.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ProductException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("LOW STOCK ALERT")) {
                    onLowStockWarning(ex);
                } else {
                    onError(ex);
                }
            }
        }
    }

    // ─── Update Product Window ─────────────────────────────────────────────

    static class UpdateProductWindow extends ActionWindow {
        private final JTextField txtId = styledField(10);
        private final JTextField txtName = styledField(18);
        private final JTextField txtPrice = styledField(10);
        private final JTextField txtQuantity = styledField(10);
        private final JComboBox<String> cmbCategory = styledCombo(
                new String[]{"General", "Grocery", "Electronic"});
        private final JTextField txtExtra = styledField(14);
        private final JLabel lblExtra = fieldLabel("Extra");

        UpdateProductWindow(InventoryGUI parent) {
            super(parent, "Update Product", 1050, 620);
            JPanel form = formPanel();

            JLabel heading = new JLabel("Update Product");
            heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
            heading.setForeground(TEXT);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(heading);

            JLabel desc = new JLabel("Load a product by ID, edit fields, then save.");
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            desc.setForeground(MUTED);
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            desc.setBorder(new EmptyBorder(0, 0, 12, 0));
            form.add(desc);

            form.add(fieldLabel("Product ID"));
            form.add(txtId);

            JPanel loadRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            loadRow.setOpaque(false);
            loadRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JButton load = actionButton("Load", PRIMARY);
            load.addActionListener(e -> loadProduct());
            loadRow.add(load);
            form.add(loadRow);

            form.add(fieldLabel("Product Name"));
            form.add(txtName);
            form.add(fieldLabel("Price ($)"));
            form.add(txtPrice);
            form.add(fieldLabel("Quantity"));
            form.add(txtQuantity);
            form.add(fieldLabel("Category"));
            form.add(cmbCategory);
            form.add(lblExtra);
            form.add(txtExtra);

            cmbCategory.addActionListener(e -> updateExtraLabel());

            localTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && localTable.getSelectedRow() >= 0) {
                    int row = localTable.getSelectedRow();
                    txtId.setText(String.valueOf(localModel.getValueAt(row, 0)));
                    loadProduct();
                }
            });

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btnRow.setOpaque(false);
            btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));
            JButton save = actionButton("Save Changes", ACCENT);
            save.addActionListener(e -> saveProduct());
            btnRow.add(save);
            form.add(btnRow);

            setContentPane(buildSplitLayout(form, "Inventory"));
        }

        private void updateExtraLabel() {
            String cat = (String) cmbCategory.getSelectedItem();
            if ("Grocery".equals(cat)) {
                lblExtra.setText("Expiry Date (YYYY-MM-DD)");
            } else if ("Electronic".equals(cat)) {
                lblExtra.setText("Warranty (months)");
            } else {
                lblExtra.setText("Extra (not needed for General)");
            }
        }

        private void loadProduct() {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                Product p = parent.manager.searchById(id);
                txtName.setText(p.getProductName());
                txtPrice.setText(String.valueOf(p.getPrice()));
                txtQuantity.setText(String.valueOf(p.getQuantity()));
                cmbCategory.setSelectedItem(p.getCategory());
                updateExtraLabel();
                if (p instanceof GroceryProduct) {
                    txtExtra.setText(((GroceryProduct) p).getExpiryDate());
                } else if (p instanceof ElectronicProduct) {
                    txtExtra.setText(String.valueOf(((ElectronicProduct) p).getWarrantyMonths()));
                } else {
                    txtExtra.setText("");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Enter a valid product ID.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (ProductException ex) {
                onError(ex);
            }
        }

        private void saveProduct() {
            try {
                Product p = buildProduct(txtId.getText(), txtName.getText(), txtPrice.getText(),
                        txtQuantity.getText(), (String) cmbCategory.getSelectedItem(),
                        txtExtra.getText(), true);
                parent.manager.updateProduct(p);
                onSuccess("Product updated successfully!");
            } catch (ProductException ex) {
                onError(ex);
            }
        }
    }
}
