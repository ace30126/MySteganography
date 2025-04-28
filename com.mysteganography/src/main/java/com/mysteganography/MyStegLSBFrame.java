package com.mysteganography;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL; // 아이콘 로드를 위해 추가
import javax.imageio.ImageIO;

public class MyStegLSBFrame extends JFrame {

    private JTextArea extractedTextArea;
    private JTextField dataTextField;
    private JLabel imageLabel;
    private JPanel imagePanel; // 이미지를 표시할 패널
    private BufferedImage currentImage; // 원본 이미지 저장
    private JProgressBar progressBar;

    // 이미지 표시 영역의 기본/선호 크기
    private static final Dimension IMAGE_AREA_PREFERRED_SIZE = new Dimension(500, 400);

    public MyStegLSBFrame() {
        setTitle("MyStegLSB");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- 아이콘 설정 ---
        try {
            // 클래스패스 루트에서 icon.png 로드
            URL iconURL = getClass().getResource("/icon.png");
            if (iconURL != null) {
                ImageIcon appIcon = new ImageIcon(iconURL);
                setIconImage(appIcon.getImage());
            } else {
                System.err.println("Warning: 아이콘 파일(/icon.png)을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
            e.printStackTrace();
        }
        // --- 아이콘 설정 끝 ---

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; // 컴포넌트가 셀을 채우도록 설정
        gbc.insets = new Insets(5, 5, 5, 5); // 컴포넌트 간 여백

        // 1행: 추출된 텍스트 영역
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3; // 버튼이 3개가 되므로 너비 3으로 변경
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        extractedTextArea = new JTextArea(5, 30);
        extractedTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(extractedTextArea);
        add(scrollPane, gbc);

        // 2행: 추가할 데이터 레이블 및 텍스트 필드
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 0.0;
        add(new JLabel("추가할 데이터:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2; // 텍스트 필드가 2칸 차지하도록 변경
        gbc.weightx = 0.9;
        dataTextField = new JTextField(20);
        add(dataTextField, gbc);

        // 3행: 인코딩, 디코딩, 초기화 버튼
        gbc.gridy = 2;
        gbc.gridwidth = 1; // 각 버튼은 1칸 차지
        gbc.weightx = 0.33; // 버튼들이 가로 크기를 균등하게 나눠 가짐
        gbc.weighty = 0.0; // 세로 크기 고정

        gbc.gridx = 0;
        JButton encodeButton = new JButton("인코딩");
        add(encodeButton, gbc);

        gbc.gridx = 1;
        JButton decodeButton = new JButton("디코딩");
        add(decodeButton, gbc);

        gbc.gridx = 2;
        JButton clearButton = new JButton("초기화"); // 초기화 버튼 추가
        add(clearButton, gbc);

        // 4행: 이미지 표시 패널 (BorderLayout 사용)
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 3; // 너비 3으로 변경
        gbc.weightx = 1.0;
        gbc.weighty = 0.7;

        imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(IMAGE_AREA_PREFERRED_SIZE);
        imagePanel.setBorder(new LineBorder(Color.GRAY));
        imagePanel.setOpaque(true);
        imagePanel.setBackground(Color.WHITE);

        imageLabel = new JLabel("이미지를 드래그 앤 드롭", SwingConstants.CENTER);
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        // DropTarget 설정
        imagePanel.setDropTarget(new DropTarget(imagePanel, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        java.util.List<File> fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (fileList != null && !fileList.isEmpty()) {
                            File imageFile = fileList.get(0);
                            loadImage(imageFile);
                        }
                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MyStegLSBFrame.this, "파일 드롭 중 오류 발생: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        }));
        add(imagePanel, gbc);

        // 5행: 프로그래스 바
        gbc.gridy = 4;
        gbc.gridwidth = 3; // 너비 3으로 변경
        gbc.weightx = 1.0;
        gbc.weighty = 0.05;
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        add(progressBar, gbc);

        // 메뉴 바 설정
        setupMenuBar();

        // 액션 리스너 설정 (초기화 버튼 추가)
        setupActionListeners(encodeButton, decodeButton, clearButton);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * 메뉴 바를 설정합니다.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("파일");
        JMenuItem openImageItem = new JMenuItem("이미지 열기");
        openImageItem.addActionListener(e -> openImage());
        fileMenu.add(openImageItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    /**
     * 이미지 열기 메뉴 항목의 액션을 처리합니다.
     */
    private void openImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("이미지 열기");
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadImage(selectedFile);
        }
    }

    /**
     * 버튼들의 액션 리스너를 설정합니다.
     * @param encodeButton 인코딩 버튼
     * @param decodeButton 디코딩 버튼
     * @param clearButton 초기화 버튼
     */
    private void setupActionListeners(JButton encodeButton, JButton decodeButton, JButton clearButton) {
        encodeButton.addActionListener(e -> encodeAction());
        decodeButton.addActionListener(e -> decodeAction());
        // 초기화 버튼 액션 리스너 추가
        clearButton.addActionListener(e -> resetImageLabel());
    }

    /**
     * 인코딩 액션을 처리합니다.
     */
    private void encodeAction() {
        if (currentImage != null && dataTextField.getText() != null && !dataTextField.getText().isEmpty()) {
            BufferedImage encodedImage = encodeLSB(currentImage, dataTextField.getText());
            if (encodedImage != null) {
                saveEncodedImage(encodedImage);
            } else {
                JOptionPane.showMessageDialog(this, "인코딩 실패! (데이터가 너무 클 수 있습니다)", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } else if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "먼저 이미지를 로드하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "인코딩할 데이터를 입력하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 인코딩된 이미지를 저장합니다.
     * @param encodedImage 인코딩된 이미지
     */
    private void saveEncodedImage(BufferedImage encodedImage) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("인코딩된 이미지 저장");
        fileChooser.setSelectedFile(new File("encoded_image.png"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                String fileName = fileToSave.getAbsolutePath();
                if (!fileName.toLowerCase().endsWith(".png")) {
                    fileName += ".png";
                    fileToSave = new File(fileName);
                }
                ImageIO.write(encodedImage, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "인코딩된 이미지가 저장되었습니다: " + fileToSave.getName(), "성공", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "이미지 저장에 실패했습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * 디코딩 액션을 처리합니다.
     */
    private void decodeAction() {
        if (currentImage != null) {
            String extractedText = decodeLSB(currentImage);
            if (extractedText != null) {
                extractedTextArea.setText(extractedText);
                JOptionPane.showMessageDialog(this, "디코딩 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "디코딩 실패 또는 숨겨진 데이터 없음.", "알림", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "먼저 디코딩할 이미지를 로드하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /**
     * 파일을 로드하고 이미지를 스케일링하여 라벨에 표시합니다.
     * @param imageFile 로드할 이미지 파일
     */
    private void loadImage(File imageFile) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            if (img != null) {
                currentImage = img; // 원본 이미지 저장

                Dimension panelSize = imagePanel.getSize();
                if (panelSize.width == 0 || panelSize.height == 0) {
                    panelSize = imagePanel.getPreferredSize();
                }
                if (panelSize.width <= 0 || panelSize.height <= 0) {
                    panelSize = IMAGE_AREA_PREFERRED_SIZE;
                }

                Image scaledImage = scaleImageToFit(currentImage, panelSize);
                ImageIcon imageIcon = new ImageIcon(scaledImage);
                imageLabel.setIcon(imageIcon);
                imageLabel.setText("");
                imageLabel.revalidate();
                imageLabel.repaint();

                // 이미지 로드 시 관련 필드 초기화 (선택 사항)
                extractedTextArea.setText("");
                dataTextField.setText("");
                progressBar.setValue(0);

            } else {
                JOptionPane.showMessageDialog(this, "이미지 파일을 읽을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                resetImageLabel();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "이미지를 로드하는 데 실패했습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            resetImageLabel();
        }
    }

    /**
     * 이미지를 대상 크기에 맞춰 비율을 유지하며 스케일링합니다.
     * @param originalImage 원본 BufferedImage
     * @param targetSize 대상 Dimension (너비, 높이)
     * @return 스케일링된 Image 객체
     */
    private Image scaleImageToFit(BufferedImage originalImage, Dimension targetSize) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int targetWidth = targetSize.width;
        int targetHeight = targetSize.height;

        if (originalWidth <= 0 || originalHeight <= 0 || targetWidth <= 0 || targetHeight <= 0) {
             return originalImage.getScaledInstance(originalWidth > 0 ? originalWidth : 1,
                                                    originalHeight > 0 ? originalHeight : 1,
                                                    Image.SCALE_SMOOTH);
        }

        double widthRatio = (double) targetWidth / originalWidth;
        double heightRatio = (double) targetHeight / originalHeight;
        double scale = Math.min(widthRatio, heightRatio);

        int newWidth = Math.max(1, (int) (originalWidth * scale));
        int newHeight = Math.max(1, (int) (originalHeight * scale));

        return originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }

    /**
     * 이미지 라벨 및 관련 상태를 초기 상태로 리셋합니다.
     */
    private void resetImageLabel() {
        imageLabel.setIcon(null);
        imageLabel.setText("이미지를 드래그 앤 드롭");
        currentImage = null;
        extractedTextArea.setText(""); // 추출된 텍스트 영역도 초기화
        dataTextField.setText("");     // 데이터 입력 필드도 초기화
        progressBar.setValue(0);      // 프로그래스 바 초기화
        imageLabel.revalidate();
        imageLabel.repaint();
    }


    // --- LSB 인코딩 및 디코딩 메서드 ---
    // (이전과 동일)
    private BufferedImage encodeLSB(BufferedImage image, String text) {
         BufferedImage imageToEncode = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
         Graphics2D g = imageToEncode.createGraphics();
         g.drawImage(image, 0, 0, null);
         g.dispose();

        byte[] textBytes = (text + "\0").getBytes();
        int totalBitsToEncode = textBytes.length * 8;
        int imageCapacity = imageToEncode.getWidth() * imageToEncode.getHeight();

        if (totalBitsToEncode > imageCapacity) {
            System.err.println("Error: Data size exceeds image capacity.");
            return null;
        }

        int bitsEncoded = 0;
        outerloop:
        for (int y = 0; y < imageToEncode.getHeight(); y++) {
            for (int x = 0; x < imageToEncode.getWidth(); x++) {
                if (bitsEncoded < totalBitsToEncode) {
                    int pixel = imageToEncode.getRGB(x, y);
                    int red = (pixel >> 16) & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue = pixel & 0xFF;

                    int byteIndex = bitsEncoded / 8;
                    int bitIndex = bitsEncoded % 8;
                    int bitToEncode = (textBytes[byteIndex] >> (7 - bitIndex)) & 1;

                    blue = (blue & 0xFE) | bitToEncode;

                    int newPixel = (pixel & 0xFF000000) | (red << 16) | (green << 8) | blue;
                    imageToEncode.setRGB(x, y, newPixel);

                    bitsEncoded++;

                    final int progress = (int) (((double) bitsEncoded / totalBitsToEncode) * 100);
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));

                } else {
                    break outerloop;
                }
            }
        }
         SwingUtilities.invokeLater(() -> progressBar.setValue(100));
        return imageToEncode;
    }

    private String decodeLSB(BufferedImage image) {
        StringBuilder decodedText = new StringBuilder();
        int currentByte = 0;
        int bitCount = 0;
        int pixelsProcessed = 0;
        int totalPixels = image.getWidth() * image.getHeight();

        SwingUtilities.invokeLater(() -> progressBar.setValue(0));

        outerloop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int blue = pixel & 0xFF;
                int lsb = blue & 1;

                currentByte = (currentByte << 1) | lsb;
                bitCount++;

                if (bitCount == 8) {
                    if (currentByte == 0) {
                        break outerloop;
                    }
                    decodedText.append((char) currentByte);
                    currentByte = 0;
                    bitCount = 0;
                }

                pixelsProcessed++;
                 final int progress = (int) (((double) pixelsProcessed / totalPixels) * 100);
                 if (pixelsProcessed % 1000 == 0) {
                     SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                 }
                 if (pixelsProcessed > totalPixels) {
                     System.err.println("Warning: Decoding processed more pixels than available.");
                     break outerloop;
                 }
            }
        }

        SwingUtilities.invokeLater(() -> progressBar.setValue(100));

        if (decodedText.length() == 0 && pixelsProcessed > 0) {
             return null;
        }

        return decodedText.toString();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Failed to set system LookAndFeel: " + e.getMessage());
            }
            new MyStegLSBFrame();
        });
    }
}
