package biz.iteksolutions.escpos.parser;

public class FormFeedCommand extends Command implements IContentOutput {

    @Override
    public String getText() {
        return "\n";
    }
}
