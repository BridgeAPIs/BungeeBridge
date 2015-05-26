package net.bridgesapis.bungeebridge.i18n;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Created by zyuiop on 26/05/15.
 * Licensed under GNU LGPL license
 */
public class TranslatorComponent extends TextComponent {
	public TranslatorComponent(TextComponent textComponent) {
		super(textComponent);
	}

	public TranslatorComponent(BaseComponent... extras) {
		super(extras);
	}

	public TranslatorComponent(String text) {
		super(I18n.getTranslation(text));
	}

	public void setTranslatedText(String text) {
		super.setText(I18n.getTranslation(text));
	}

	public void addTranslatedExtra(String text) {
		super.addExtra(I18n.getTranslation(text));
	}
}
