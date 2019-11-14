package Suchen;

public enum SearchType {
    ICD(" ICD-10-Text","Code eingeben ->"),
    TEXT(" ICD-10-Code","Text eingeben ->");

    String display;
    String label;

    private SearchType(String cmbBoxText, String lblText) {
        display = cmbBoxText;
        label = lblText;
    }

    @Override
    public String toString() {
        return display;
    }
    
    public String label() {
        return label;
    }
}
