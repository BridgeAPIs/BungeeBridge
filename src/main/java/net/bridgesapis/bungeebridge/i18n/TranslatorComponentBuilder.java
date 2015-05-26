package net.bridgesapis.bungeebridge.i18n;

import net.md_5.bungee.api.chat.ComponentBuilder;

/**
 * Created by zyuiop on 26/05/15.
 * Licensed under GNU LGPL license
 */
public class TranslatorComponentBuilder extends ComponentBuilder {
	public TranslatorComponentBuilder(String text) {
		super(I18n.getTranslation(text));
	}

	public ComponentBuilder appendTranslatedText(String text) {
		return super.append(I18n.getTranslation(text));
	}

	public ComponentBuilder appendTranslatedText(String text, FormatRetention retention) {
		return super.append(I18n.getTranslation(text), retention);
	}
}
