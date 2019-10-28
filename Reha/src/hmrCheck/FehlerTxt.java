package hmrCheck;

public class FehlerTxt {
    String htmlTxt;

    public FehlerTxt() {
        htmlTxt = "";
    }

    public void add(String txt) {
        htmlTxt = htmlTxt + (htmlTxt.length() <= 0 ? "<html>" : "") + txt;
    }
    
    public String getTxt() {
        htmlTxt = htmlTxt + (htmlTxt.length() > 0 ? "</html>" : "");
        return htmlTxt;
    }
}
