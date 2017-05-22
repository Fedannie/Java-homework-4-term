package logic;

import java.util.ArrayList;
import java.util.Random;

public class Field {
    private ArrayList<ArrayList<Integer>> numbers;
    private final int N;
    private int chosen_x, chosen_y;
    private boolean was_chosen = false;

    public Field(int n) throws Exception{
        N = n;
        if (N % 2 != 0) {
            throw new Exception("N is not correct.");
        }

        Random rand = new Random();
        numbers = new ArrayList<ArrayList<Integer>>(N);
        for (int i = 0; i < N; i++) {
            numbers.add(new ArrayList<Integer>(N));
            for (int j = 0; j < N; j++) {
                numbers.get(i).add(j, 0);
            }
        }

        for (int i = 0; i < N * N / 2; i++) {
            int x = rand.nextInt(N);
            int y = rand.nextInt(N);
            while (numbers.get(x).get(y) == 1){
                x = rand.nextInt(N);
                y = rand.nextInt(N);
            }
            numbers.get(x).set(y, 1);
        }
    }

    public int firstMove(int x, int y) throws Exception{
        if (!check(x, y)){
            return -1;
        }
        chosen_x = x;
        chosen_y = y;
        return numbers.get(x).get(y);
    }

    public int secondMove(int x, int y) throws Exception{
        if (!check(x, y)) {
            return -1;
        }
        if (chosen_y == y && chosen_x == x){
                return 2;
        }
        if (numbers.get(x).get(y).equals(numbers.get(chosen_x).get(chosen_y))) {
            return 1;
        }
        return 0;
    }

    private boolean check(int x, int y) throws Exception{
        if (x < 0 || x >= N || y < 0 || y >= N) {
            throw new Exception("Check coordinates");
        }
        if (numbers.get(x).get(y) == -1) {
            return false;
        }
        was_chosen = !was_chosen;
        return true;
    }

}
