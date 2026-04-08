package gui;

import service.TaskService;
import model.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;

public class TodoFrame extends JFrame {

    // ── Palette ────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(13, 13, 23);
    private static final Color BG_CARD       = new Color(22, 22, 38);
    private static final Color BG_INPUT      = new Color(30, 30, 50);
    private static final Color ACCENT        = new Color(99, 102, 241);
    private static final Color ACCENT_HOVER  = new Color(129, 132, 255);
    private static final Color SUCCESS       = new Color(52, 211, 153);
    private static final Color DANGER        = new Color(248, 113, 113);
    private static final Color DANGER_HOVER  = new Color(255, 143, 143);
    private static final Color TEXT_PRIMARY  = new Color(240, 240, 255);
    private static final Color TEXT_MUTED    = new Color(120, 120, 160);
    private static final Color BORDER        = new Color(45, 45, 70);

    private final TaskService service = new TaskService();
    private final DefaultListModel<Task> listModel = new DefaultListModel<>();
    private JList<Task> taskList;
    private JTextField inputField;
    private JLabel countLabel;
    private float orbAngle = 0f;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public TodoFrame() {
        setTitle("Taskflow");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 680);
        setLocationRelativeTo(null);
        setResizable(true);
        setUndecorated(true);
        initUI();
        startOrbAnimation();
    }

    private void initUI() {
        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout(0, 0));
        setContentPane(root);
        root.add(buildTitleBar(),  BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);
        root.add(buildInputArea(), BorderLayout.SOUTH);

        // Drag-to-move (only when not maximized)
        MouseAdapter drag = new MouseAdapter() {
            Point origin;
            public void mousePressed(MouseEvent e)  { origin = e.getPoint(); }
            public void mouseDragged(MouseEvent e)  {
                if (isMaximized) return;
                Point p = TodoFrame.this.getLocation();
                setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
            }
        };
        root.addMouseListener(drag);
        root.addMouseMotionListener(drag);

        // Resize handle (bottom-right corner)
        ResizeHandle resizeHandle = new ResizeHandle();
        root.setLayout(new BorderLayout());
        root.add(buildTitleBar(),  BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);

        JPanel southWrapper = new JPanel(new BorderLayout());
        southWrapper.setOpaque(false);
        southWrapper.add(buildInputArea(), BorderLayout.CENTER);
        southWrapper.add(resizeHandle,     BorderLayout.EAST);
        root.add(southWrapper, BorderLayout.SOUTH);
    }

    private void toggleMaximize() {
        if (!isMaximized) {
            normalBounds = getBounds();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle screen = ge.getMaximumWindowBounds();
            setBounds(screen);
            isMaximized = true;
        } else {
            setBounds(normalBounds);
            isMaximized = false;
        }
        revalidate(); repaint();
    }

    // ── Title Bar ──────────────────────────────────────────
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(18, 18, 32));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER);
                g2.fillRect(0, getHeight()-1, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 56));
        bar.setBorder(new EmptyBorder(0, 20, 0, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel dot = new JLabel("◆");
        dot.setFont(new Font("Serif", Font.BOLD, 18));
        dot.setForeground(ACCENT);
        JLabel title = new JLabel("Taskflow");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        left.add(dot); left.add(title);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);
        controls.add(windowBtn(new Color(255, 189, 68), e -> setState(ICONIFIED)));
        controls.add(windowBtn(new Color(39, 201, 120),  e -> toggleMaximize()));
        controls.add(windowBtn(DANGER, e -> System.exit(0)));

        bar.add(left, BorderLayout.WEST);
        bar.add(controls, BorderLayout.EAST);

        // Double-click title bar to maximize/restore
        bar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) toggleMaximize();
            }
        });
        return bar;
    }

    private JButton windowBtn(Color col, ActionListener al) {
        JButton btn = new JButton() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? col.brighter() : col);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(14, 14));
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    // ── Center ─────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 8, 20));
        panel.add(buildStatsBar(), BorderLayout.NORTH);
        panel.add(buildTaskList(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        countLabel = new JLabel("No tasks yet");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        countLabel.setForeground(TEXT_MUTED);
        JLabel hint = new JLabel("double-click to complete");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(70, 70, 100));
        bar.add(countLabel, BorderLayout.WEST);
        bar.add(hint, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTaskList() {
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCellRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setOpaque(false);
        taskList.setBackground(new Color(0,0,0,0));
        taskList.setFixedCellHeight(62);
        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) markDone();
            }
        });

        JScrollPane scroll = new JScrollPane(taskList) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setOpaque(false);
        return scroll;
    }

    // ── Input Area ─────────────────────────────────────────
    private JPanel buildInputArea() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 20, 20, 20));

        inputField = new JTextField() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (getText().isEmpty() && !hasFocus()) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.setColor(TEXT_MUTED);
                    g2.drawString("Add a new task…", 16, 29);
                }
                // Focus ring
                if (hasFocus()) {
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 80));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setForeground(TEXT_PRIMARY);
        inputField.setCaretColor(ACCENT);
        inputField.setOpaque(false);
        inputField.setBorder(new EmptyBorder(0, 16, 0, 16));
        inputField.setPreferredSize(new Dimension(0, 46));
        inputField.addActionListener(e -> addTask());

        JPanel btnRow = new JPanel(new GridLayout(1, 3, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(glowButton("＋  Add Task",  ACCENT,   ACCENT_HOVER,  e -> addTask()));
        btnRow.add(glowButton("✔  Mark Done",  SUCCESS,  SUCCESS.darker(), e -> markDone()));
        btnRow.add(glowButton("✕  Delete",     DANGER,   DANGER_HOVER,   e -> deleteTask()));

        wrapper.add(inputField, BorderLayout.NORTH);
        wrapper.add(btnRow,     BorderLayout.SOUTH);
        return wrapper;
    }

    // ── Actions ────────────────────────────────────────────
    private void addTask() {
        try {
            service.addTask(inputField.getText());
            refreshList();
            inputField.setText("");
            inputField.requestFocus();
        } catch (IllegalArgumentException ex) {
            shake(inputField);
        }
    }

    private void markDone() {
        int idx = taskList.getSelectedIndex();
        if (idx < 0) { pulseList(); return; }
        service.markCompleted(idx);
        refreshList();
    }

    private void deleteTask() {
        int idx = taskList.getSelectedIndex();
        if (idx < 0) { pulseList(); return; }
        int c = JOptionPane.showConfirmDialog(this, "Delete this task?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) { service.deleteTask(idx); refreshList(); }
    }

    private void refreshList() {
        listModel.clear();
        List<Task> tasks = service.getAllTasks();
        for (Task t : tasks) listModel.addElement(t);
        long done = tasks.stream().filter(Task::isCompleted).count();
        if (tasks.isEmpty()) { countLabel.setText("No tasks yet"); return; }
        countLabel.setText(tasks.size() + " task" + (tasks.size()!=1?"s":"")
                + (done > 0 ? "  ·  " + done + " completed" : ""));
    }

    private void shake(JComponent c) {
        Point orig = c.getLocation();
        int[] steps = {-8,8,-6,6,-4,4,-2,2,0};
        final int[] i = {0};
        Timer t = new Timer(30, null);
        t.addActionListener(e -> {
            if (i[0] >= steps.length) { t.stop(); c.setLocation(orig); return; }
            c.setLocation(orig.x + steps[i[0]++], orig.y);
        });
        t.start();
    }

    private void pulseList() {
        taskList.setBackground(new Color(80, 30, 30));
        new Timer(220, e -> { taskList.setBackground(new Color(0,0,0,0)); ((Timer)e.getSource()).stop(); }).start();
    }

    private void startOrbAnimation() {
        new Timer(16, e -> { orbAngle = (orbAngle + 0.4f) % 360f; repaint(); }).start();
    }

    // ── Helpers ────────────────────────────────────────────
    private JButton glowButton(String label, Color base, Color hover, ActionListener al) {
        JButton btn = new JButton(label) {
            boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov=true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hov=false; repaint(); }
            }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = hov ? hover : base;
                if (hov) {
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 55));
                    g2.fillRoundRect(-4,-4,getWidth()+8,getHeight()+8,18,18);
                }
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    // ══════════════════════════════════════════════════════
    //  INNER CLASSES
    // ══════════════════════════════════════════════════════

    class BackgroundPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Base gradient
            g2.setPaint(new GradientPaint(0,0,BG_DARK, getWidth(),getHeight(),new Color(18,12,35)));
            g2.fillRect(0,0,getWidth(),getHeight());
            // Floating orbs
            double rad = Math.toRadians(orbAngle);
            drawOrb(g2,(int)(getWidth()*0.15+40*Math.cos(rad)),(int)(getHeight()*0.25+30*Math.sin(rad)),180,new Color(99,102,241,35));
            drawOrb(g2,(int)(getWidth()*0.80+35*Math.cos(rad+Math.PI)),(int)(getHeight()*0.15+25*Math.sin(rad+Math.PI)),140,new Color(52,211,153,25));
            drawOrb(g2,(int)(getWidth()*0.70+30*Math.cos(rad*0.7)),(int)(getHeight()*0.80+20*Math.sin(rad*0.7)),120,new Color(248,113,113,20));
            // Window border
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
            g2.dispose();
        }
        private void drawOrb(Graphics2D g2, int cx, int cy, int r, Color c) {
            g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,1f},new Color[]{c,new Color(0,0,0,0)}));
            g2.fillOval(cx-r,cy-r,r*2,r*2);
        }
    }

    class TaskCellRenderer extends JPanel implements ListCellRenderer<Task> {
        private Task task; private boolean selected;
        TaskCellRenderer() { setOpaque(false); }
        public Component getListCellRendererComponent(JList<? extends Task> list,
                                                      Task value, int index, boolean isSelected, boolean cellHasFocus) {
            this.task = value; this.selected = isSelected; return this;
        }
        protected void paintComponent(Graphics g) {
            if (task == null) return;
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int pad = 8, h = getHeight()-pad;
            // Card
            g2.setColor(selected ? new Color(50,50,80) : new Color(28,28,46));
            g2.fillRoundRect(pad, pad/2, getWidth()-pad*2, h-2, 12, 12);
            // Left accent bar
            g2.setColor(task.isCompleted() ? SUCCESS : ACCENT);
            g2.fillRoundRect(pad, pad/2, 4, h-2, 4, 4);
            // Checkbox
            int cx=pad+24, cy=getHeight()/2;
            g2.setStroke(new BasicStroke(1.8f));
            g2.setColor(task.isCompleted() ? SUCCESS : BORDER);
            g2.drawOval(cx-9,cy-9,18,18);
            if (task.isCompleted()) { g2.setColor(SUCCESS); g2.fillOval(cx-5,cy-5,10,10); }
            // Title
            int textX = cx+20;
            g2.setFont(new Font("Segoe UI", task.isCompleted()?Font.PLAIN:Font.BOLD, 14));
            g2.setColor(task.isCompleted() ? TEXT_MUTED : TEXT_PRIMARY);
            g2.drawString(task.getTitle(), textX, cy+5);
            // Strikethrough
            if (task.isCompleted()) {
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(TEXT_MUTED); g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(textX, cy+1, textX+fm.stringWidth(task.getTitle()), cy+1);
            }
            // Badge
            if (task.isCompleted()) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                int bw=fm.stringWidth("Done")+12, bx=getWidth()-pad*2-bw, by=cy-9;
                g2.setColor(new Color(52,211,153,40)); g2.fillRoundRect(bx,by,bw,18,8,8);
                g2.setColor(SUCCESS); g2.drawString("Done",bx+6,by+13);
            }
            g2.dispose();
        }
        public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width,62); }
    }

    /** Draggable resize grip at bottom-right corner */
    class ResizeHandle extends JPanel {
        ResizeHandle() {
            setPreferredSize(new Dimension(16, 16));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            MouseAdapter ma = new MouseAdapter() {
                Point start; Rectangle startBounds;
                public void mousePressed(MouseEvent e) {
                    start = e.getLocationOnScreen();
                    startBounds = TodoFrame.this.getBounds();
                }
                public void mouseDragged(MouseEvent e) {
                    if (isMaximized) return;
                    Point cur = e.getLocationOnScreen();
                    int dw = cur.x - start.x, dh = cur.y - start.y;
                    int nw = Math.max(400, startBounds.width  + dw);
                    int nh = Math.max(500, startBounds.height + dh);
                    TodoFrame.this.setSize(nw, nh);
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(80, 80, 130, 120));
            for (int i = 0; i < 3; i++) {
                int off = i * 4;
                g2.fillRect(getWidth()-4-off, getHeight()-4, 3, 3);
                g2.fillRect(getWidth()-4,     getHeight()-4-off, 3, 3);
            }
            g2.dispose();
        }
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        protected void configureScrollBarColors() { thumbColor=new Color(80,80,130); trackColor=new Color(0,0,0,0); }
        protected JButton createDecreaseButton(int o) { return emptyBtn(); }
        protected JButton createIncreaseButton(int o) { return emptyBtn(); }
        private JButton emptyBtn() { JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor); g2.fillRoundRect(r.x+2,r.y+2,r.width-4,r.height-4,6,6); g2.dispose();
        }
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {}
    }
}