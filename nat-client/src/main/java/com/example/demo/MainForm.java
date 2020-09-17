package com.example.demo;

import com.example.demo.codec.MessageDecoder;
import com.example.demo.codec.MessageEncoder;
import com.example.demo.handler.ClientHandler;
import com.example.demo.net.TcpClient;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

@Slf4j
public class MainForm extends JFrame {

    private static MainForm instance;

    public static synchronized MainForm getInstance() {
        if (instance == null) {
            instance = new MainForm();
        }
        return instance;
    }

    private static TcpClient client;

    private static JTextField remoteHost;
    private static JTextField remotePort;
    private static JTextField localPort;
    private static JTextField proxyPort;
    private static JTextArea txtConsole;
    private static JScrollPane panelConsole;
    private static JButton btn;

    public MainForm() {
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/icon.png"));
        setIconImage(imageIcon.getImage());
        setTitle("内网穿透客户端");
        setSize(400, 400);
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);

        JPanel settingPanel = new JPanel();
        settingPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "网络参数", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        settingPanel.setBounds(20, 10, 360, 80);
        getContentPane().add(settingPanel);
        settingPanel.setLayout(null);

        JLabel remoteText = new JLabel("绑定域名：");
        remoteText.setBounds(20, 20, 80, 20);
        settingPanel.add(remoteText);

        remoteHost = new JTextField();
        remoteHost.setText("127.0.0.1");
        remoteHost.setBounds(80, 20, 140, 20);
        settingPanel.add(remoteHost);

        remotePort = new JTextField();
        remotePort.setText("8888");
        remotePort.setBounds(220, 20, 80, 20);
        settingPanel.add(remotePort);

        JLabel localText = new JLabel("本地端口：");
        localText.setBounds(20, 40, 80, 20);
        settingPanel.add(localText);

        localPort = new JTextField();
        localPort.setText("8080");
        localPort.setBounds(80, 40, 80, 20);
        settingPanel.add(localPort);

        JLabel proxyText = new JLabel("代理端口：");
        proxyText.setBounds(160, 40, 80, 20);
        settingPanel.add(proxyText);

        proxyPort = new JTextField();
        proxyPort.setText("10000");
        proxyPort.setBounds(220, 40, 80, 20);
        settingPanel.add(proxyPort);

        txtConsole = new JTextArea() {
            @Override
            public void append(String str) {
                this.setCaretPosition(this.getDocument().getLength());
                str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + " - " + str;
                if (this.getText().length() > 100000) {
                    this.setText("");
                }
                super.append(str);
            }
        };
        txtConsole.setText("https://github.com/littleredhat1997/NAT\n");
        panelConsole = new JScrollPane(txtConsole, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panelConsole.setLocation(20, 100);
        getContentPane().add(panelConsole);

        btn = new JButton("启动服务");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btn.getText().equals("启动服务")) {
                    connect();
                } else {
                    close();
                }
            }
        });
        getContentPane().add(btn);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = MainForm.this.getWidth() - 40;
                int height = MainForm.this.getHeight() - 80;
                settingPanel.setSize(width, settingPanel.getHeight());
                panelConsole.setSize(width, height - panelConsole.getY());
                btn.setSize(width, 20);
                btn.setLocation(20, height);
                super.componentResized(e);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
                System.exit(0);
                super.windowClosing(e);
            }
        });
    }

    public static void main(String[] args) {
        MainForm.getInstance().setVisible(true);
    }

    private static void connect() {
        if (remoteHost == null || remoteHost.getText().equals("")
                || remotePort == null || remotePort.getText().equals("")
                || localPort == null || localPort.getText().equals("")
                || proxyPort == null || proxyPort.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "请输入完整信息！", "提示消息", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String serverHost = remoteHost.getText();
        int serverPort = Integer.parseInt(remotePort.getText());
        int local = Integer.parseInt(localPort.getText());
        int proxy = Integer.parseInt(proxyPort.getText());

        client = new TcpClient();
        try {
            client.connect(serverHost, serverPort, new ChannelInitializer<SocketChannel>() { // 8888
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            // 拆包粘包
                            new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                            new MessageDecoder(),
                            new MessageEncoder(),
                            // 心跳检测
                            new IdleStateHandler(0, 30, 0),
                            new ClientHandler(proxy, "127.0.0.1", local)
                    );
                }
            });
            log.info("连接服务器：" + serverHost + ":" + serverPort);
            txtConsole.append("连接服务器：" + serverHost + ":" + serverPort + "\n");
        } catch (Exception e) {
            log.error("无法连接服务器");
            txtConsole.append("无法连接服务器\n");
        }
    }

    private static void close() {
        if (client != null) client.close();
    }

    public void stop() {
        btn.setText("停止服务");
    }

    public void start() {
        btn.setText("启动服务");
    }

    public void showMessage(String message) {
        txtConsole.append(message + "\n");
    }
}
