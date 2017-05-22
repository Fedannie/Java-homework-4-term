package gui;

import logic.Field;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MemoApp extends JFrame{
    private Field field;
    public static final int N = 10;
    private boolean was_pressed = false;
    private int activated_button;
    private Map<Integer, JButton> buttons = new HashMap<Integer, JButton>();
    private Color unenable = new Color(253, 143, 173);
    private Color found = new Color(143, 253, 198);
    private Color standart = new Color(245, 255, 142);
    private int rest = N*N;

    public MemoApp() {
        try{
            field = new Field(N);
        } catch (Exception e){
            System.out.println("Oops. Exception: " + e.getMessage());
        }

        init();
    }

    private void init(){
        Container container = this.getContentPane();
        container.setBackground(new Color(229, 229, 229));
        container.setLayout(new GridLayout(N, N));

        for (int i = 0; i < N; i++){
            for (int j = 0; j < N; j++){
                JButton button = new JButton();
                button.setName("" + (i * N + j));
                container.add(button);
                buttons.put(i * N + j, button);
                button.setBackground(standart);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            JButton o = (JButton)e.getSource();
                            int x = Integer.parseInt(o.getName()) / N;
                            int y = Integer.parseInt(o.getName()) % N;
                            if (!was_pressed){
                                activated_button = x * N + y;
                                int result = field.firstMove(x, y);
                                if (result != -1){
                                    o.setText("" + result);
                                    o.setBackground(unenable);
                                    o.setEnabled(false);
                                }
                            } else {
                                int result = field.secondMove(x, y);
                                parseSecondResult(result, x * N + y);
                            }
                            was_pressed = !was_pressed;
                        } catch (Exception ex) {}
                        checkFinish();
                    }
                });
            }
        }
    }

    public void checkFinish(){
        if (rest == 0){
            JOptionPane.showMessageDialog(this.getContentPane(),
                    "Congratulations! You've finished the game.");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e){}
            getContentPane().removeAll();
            init();
        }
    }

    public void parseSecondResult(int result, int secondButton){
        JButton first = buttons.get(activated_button);
        JButton second = buttons.get(secondButton);
        if (result == -1){
            return;
        }
        if (result == 2 || result == 0){
            second.setBackground(unenable);
            if (result == 0) {
                second.setText("" + (1 - Integer.parseInt(first.getText())));
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e){}
            clearButton(activated_button);
            clearButton(secondButton);
        } else {
            second.setText(first.getText());
            first.setBackground(found);
            second.setBackground(found);
            first.setEnabled(false);
            second.setEnabled(false);
            rest -= 2;
        }
    }

    private void clearButton(int num){
        JButton button = buttons.get(num);
        button.setEnabled(true);
        button.setBackground(standart);
        button.setText("");
    }

    public static void main(String[] args) {
        MemoApp app = new MemoApp();
        app.setVisible(true);
    }
}
