package opRgaf;


enum Strategy {
    gleich {

        @Override
        <T extends Comparable<T>> boolean  compare(Comparable<T> first, T second) {

            if (first == null) {
                return second == null;
            }
            return first.compareTo(second) == 0;
        }
    },
    kleiner {
        @Override
        <T extends Comparable<T>> boolean  compare(Comparable<T> first, T second) {
            return first.compareTo(second) < 0;
        }
    },
    groesser {
        @Override
        <T extends Comparable<T>> boolean  compare(Comparable<T> first, T second) {

            return first.compareTo(second) > 0;
        }
    },
    kleinerOderGleich {
        @Override
        <T extends Comparable<T>> boolean  compare(Comparable<T> first, T second) {
            return first.compareTo(second) <= 0;
        }
    },
    groesserOderGleich

    {
        @Override
        <T extends Comparable<T>> boolean  compare(Comparable<T> first, T second) {
            return first.compareTo(second) >= 0;
        }
    },
    ungleich {
        @Override
        <T extends Comparable<T>> boolean  compare(Comparable<T> first, T second) {
            return first.compareTo(second) != 0;
        }
    };

    abstract <T extends Comparable<T>> boolean  compare(Comparable<T> first, T second);
}
