package com.micro.mcpt;

import com.mcxiaoke.packer.helper.PackerNg;
import com.micro.mcpt.util.ConfigUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * APK多渠道读写工具
 *
 * @author act262@gmail.com
 */
public class ToolGui {
    private JButton mApkFileButton;
    private JPanel mRootPannel;
    private JTextField mApkFilePath;
    private JTextArea mConsoleArea;
    private JButton mBeginButton;
    private JTextField mMarketFilePath;
    private JButton mMarketButton;
    private JProgressBar progressBar1;
    private JScrollPane mScrollText;

    public ToolGui() {
        // 初始化上次记录的文件位置
        mApkFilePath.setText(getLastApkFilePath());
        mMarketFilePath.setText(getLastMarketFilePath());

        mApkFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openApkFile();
            }
        });

        mMarketButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openMarketFile();
            }
        });

        mBeginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                begin();
            }
        });
    }

    // 开始处理
    private void begin() {
        String apkFilePath = mApkFilePath.getText();
        // 未选择文件
        if (apkFilePath == null || apkFilePath.isEmpty()) {
            prompt("APK文件不存在");
            return;
        }
        String marketFilePath = mMarketFilePath.getText();
        if (marketFilePath == null || marketFilePath.isEmpty()) {
            prompt("Market文件不存在");
            return;
        }

        try {
            boolean check = check(apkFilePath);
            if (!check) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 文件操作发生错误
            mConsoleArea.setText("文件不存在");
            return;
        }

        // 保存上次有效目录
        save(apkFilePath, marketFilePath);

        try {
            build(apkFilePath, marketFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            prompt("构建失败");
        }
    }

    // 打开市场文件
    private void openMarketFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Market Text Files", "txt"));
        // 记录上次选择位置
        File file = new File(mMarketFilePath.getText());
        if (file.exists()) {
            chooser.setCurrentDirectory(file);
        }
        int returnVal = chooser.showOpenDialog(mRootPannel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            mMarketFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    /**
     * 打开Apk文件
     */
    private void openApkFile() {
        // 打开APK文件
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Android Apk Files", "apk"));
        // 记录上次选择位置
        File file = new File(mApkFilePath.getText());
        if (file.exists()) {
            chooser.setCurrentDirectory(file);
        }

        int returnVal = chooser.showOpenDialog(mRootPannel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            mApkFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    /**
     * 弹出提示框
     *
     * @param message 　提示信息
     */
    private void prompt(String message) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, message, "WARNING", JOptionPane.ERROR_MESSAGE);
    }

    // 检查APK文件是否已经写入过数据
    private boolean check(String filePath) throws IOException {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(filePath);
            String name = zipFile.getName();
            String comment = zipFile.getComment();
            int size = zipFile.size();
            System.out.println("name = " + name);
            System.out.println("size = " + size);
            System.out.println("comment = " + comment);

            if (comment != null) {
                prompt("该APK文件已经写入过信息");
                return false;
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }
        return true;
    }

    /**
     * 开始构建
     */
    private void build(String apkFilePath, String marketFilePath) throws Exception {
        mConsoleArea.setText("");
        mConsoleArea.setText("build start\n");
        File apkFile = new File(apkFilePath);
        if (!apkFile.exists()) {
            prompt("APK 文件不存在");
            return;
        }

        File marketFile = new File(marketFilePath);
        if (!marketFile.exists()) {
            prompt("Market 文件不存在");
            return;
        }

        List<String> markets = PackerNg.Helper.parseMarkets(marketFile);
        if (markets == null || markets.isEmpty()) {
            prompt("Market NULL");
            return;
        }

        // output dir
        final File outputDir = new File("apks");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        } else {
            PackerNg.Helper.deleteDir(outputDir);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String baseName = PackerNg.Helper.getBaseName(apkFile.getName());
                String extName = PackerNg.Helper.getExtension(apkFile.getName());

                // 处理进度
                int processed = 0;
                progressBar1.setMaximum(markets.size());
                progressBar1.setValue(processed);
                for (final String market : markets) {
                    final String apkName = baseName + "-" + market + "." + extName;
                    File destFile = new File(outputDir, apkName);
                    try {
                        PackerNg.Helper.copyFile(apkFile, destFile);
                        PackerNg.Helper.writeMarket(destFile, market);
                        if (PackerNg.Helper.verifyMarket(destFile, market)) {
                            ++processed;
                            mConsoleArea.append("processed apk " + apkName);
                        } else {
                            destFile.delete();
                            mConsoleArea.append("failed to process " + apkName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    progressBar1.setValue(processed);
                    long percentage = Math.round(progressBar1.getValue() * 100.0 / progressBar1.getMaximum());
                    progressBar1.setString(percentage + "%");
                    mConsoleArea.append("\n");
                }
                mConsoleArea.append("all " + processed + " processed apks saved to " + outputDir);
                mConsoleArea.append("\n");
                mConsoleArea.append("build successful");
            }
        }).start();
    }

    private void handleProcess() {

    }

    //　保存配置信息
    private void save(String apkFilePath, String marketFilePath) {
        ConfigUtil.put(APK_PATH, apkFilePath);
        ConfigUtil.put(MARKET_PATH, marketFilePath);
    }

    private String getLastApkFilePath() {
        return ConfigUtil.get(APK_PATH);
    }

    private String getLastMarketFilePath() {
        return ConfigUtil.get(MARKET_PATH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                invokeUI();
            }
        });
    }

    private static void invokeUI() {
        JFrame frame = new JFrame("ToolGui");
        frame.setContentPane(new ToolGui().mRootPannel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Android 多渠道构建工具");
        frame.pack();
        frame.setResizable(false);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static final String APK_PATH = "apkFilePath";
    public static final String MARKET_PATH = "marketFilePath";
}
