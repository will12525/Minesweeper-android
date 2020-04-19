package com.lawrence.sweeper;
//easy; bombs: 10, (9, 9)
//medium; bombs: 40, (16, 16)
//hard; bombs: 99, (30, 16)
class difficulties {

    //EASY(new int[]{0, 10, 9, 9}), MEDIUM(new int[]{1, 40, 16, 16}), HARD(new int[]{2, 99, 30, 16});

    //0: difficulty, 1: bombs, 2: width, 3: height
    private int[] easy = {0, 10, 9, 9};
    private int[] medium = {1, 40, 16, 16};
    private int[] hard = {2, 99, 16, 30};

    int[] getDifficulty(int difficulty){
        switch (difficulty){
            case 0:
                return easy;
            case 1:
                return medium;
            case 2:
                return hard;
            default:
                return null;
        }
    }
}


