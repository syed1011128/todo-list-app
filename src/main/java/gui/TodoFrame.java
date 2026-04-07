package gui;

import service.TaskService;
import model.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TodoFrame extends JFrame {

    private final TaskService service = new TaskService();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> taskList = new JList<>(listModel);
    private final JTextField inputField = new JTextField();

    public TodoFrame() {
        setTitle("To-Do List Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));
        root.setBackground(new Color(245, 245, 250));

        // ── Title ──────────────────────────────────────────
        JLabel title = new JLabel("📝 My To-Do List", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(50, 50, 120));
        root.add(title, BorderLayout.NORTH);

        // ── Task list ──────────────────────────────────────
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setBackground(Color.WHITE);
        taskList.setFixedCellHeight(30);

        JScrollPane scroll = new JScrollPane(taskList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 220)));
        root.add(scroll, BorderLayout.CENTER);

        // ── Input panel ────────────────────────────────────
        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setBackground(new Color(245, 245, 250));

        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setPreferredSize(new Dimension(0, 36));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 210)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        inputField.addActionListener(e -> addTask());   // Enter key support

        JButton addBtn = styledButton("Add", new Color(70, 130, 180));
        addBtn.addActionListener(e -> addTask());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(addBtn, BorderLayout.EAST);

        // ── Action buttons ─────────────────────────────────
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        btnPanel.setBackground(new Color(245, 245, 250));

        JButton doneBtn   = styledButton("✔ Mark Done",  new Color(60, 160, 90));
        JButton deleteBtn = styledButton("🗑 Delete",     new Color(200, 70, 70));
        JButton exitBtn   = styledButton("✖ Exit",       new Color(120, 120, 140));

        doneBtn.addActionListener(e   -> markDone());
        deleteBtn.addActionListener(e -> deleteTask());
        exitBtn.addActionListener(e   -> System.exit(0));

        btnPanel.add(doneBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(exitBtn);

        // ── Bottom area ────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBackground(new Color(245, 245, 250));
        bottom.add(inputPanel, BorderLayout.NORTH);
        bottom.add(btnPanel,   BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);

        add(root);
    }

    // ── Actions ────────────────────────────────────────────

    private void addTask() {
        String text = inputField.getText();
        try {
            service.addTask(text);
            refreshList();
            inputField.setText("");
            inputField.requestFocus();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void markDone() {
        int idx = taskList.getSelectedIndex();
        if (idx < 0) { showNoSelection(); return; }
        service.markCompleted(idx);
        refreshList();
    }

    private void deleteTask() {
        int idx = taskList.getSelectedIndex();
        if (idx < 0) { showNoSelection(); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this task?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteTask(idx);
            refreshList();
        }
    }

    private void refreshList() {
        listModel.clear();
        List<Task> tasks = service.getAllTasks();
        for (Task t : tasks) listModel.addElement(t.toString());
    }

    private void showNoSelection() {
        JOptionPane.showMessageDialog(this, "Please select a task first.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Helper ─────────────────────────────────────────────

    private JButton styledButton(String label, Color bg) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(0, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}