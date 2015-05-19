package net.zyuiop.bungeebridge.core.players;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PlayerDataAbstract {

	protected Map<String, String> playerData = new ConcurrentHashMap<>();
	protected Date lastRefresh;
	protected final UUID playerID;

	protected PlayerDataAbstract(UUID playerID) {
		this.playerID = playerID;
	}

	/**
	 * Permet d'obtenir l'UUID du joueur
	 * @return UUID du joueur
	 */
	public UUID getPlayerID() {
		return playerID;
	}

	/**
	 * Renvoie la dernière date d'actualisation depuis la base de données
	 * @return Dernière actualisation
	 */
	public Date getLastRefresh() {
		return lastRefresh;
	}

	/**
	 * Obtient les clés des données stockées
	 * @return Liste des clés stockées
	 */
	public Set<String> getKeys() {
		return playerData.keySet();
	}

	/**
	 * Obtient l'ensemble des données du joueur
	 * @return données du joueur
	 */
	public Map<String, String> getValues() {
		return playerData;
	}

	/**
	 * Permet de savoir si les données du joueur contiennent une clé en particulier
	 * @param key Clé à tester
	 * @return true si cette clé existe
	 */
	public boolean contains(String key) {
		return playerData.containsKey(key);
	}

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @return valeur de la clé, null si elle n'existe pas
	 */
	public String get(String key) {
		return playerData.get(key);
	}

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @param def Valeur par défaut
	 * @return valeur de la clé, <code>def</code> si elle n'existe pas
	 */
	public String get(String key, String def) {
		return (contains(key) ? get(key) : def);
	}

	/**
	 * Définit une valeur dans les données du joueur
	 * @param key	Clé à définir
	 * @param value Valeur à définir
	 */
	public abstract void set(String key, String value);

	public abstract void remove(String key);

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @return valeur de la clé, null si elle n'existe pas
	 * @throws net.samagames.api.player.InvalidTypeException si la valeur n'est pas du bon type
	 */
	public Integer getInt(String key) {
		String val = get(key);
		if (val == null)
			return null;

		try {
			return Integer.valueOf(val);
		} catch (Exception e) {
			throw new InvalidTypeException("This value is not an int.");
		}
	}

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @param def Valeur par défaut
	 * @return valeur de la clé, <code>def</code> si elle n'existe pas
	 * @throws net.samagames.api.player.InvalidTypeException si la valeur n'est pas du bon type
	 */
	public Integer getInt(String key, int def) {
		Integer ret = getInt(key);
		if (ret == null)
			return def;
		else
			return ret;
	}

	/**
	 * Définit une valeur dans les données du joueur
	 * @param key	Clé à définir
	 * @param value Valeur à définir
	 */
	public abstract void setInt(String key, int value);

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @return valeur de la clé, null si elle n'existe pas
	 * @throws net.samagames.api.player.InvalidTypeException si la valeur n'est pas du bon type
	 */
	public Boolean getBoolean(String key) {
		String val = get(key);
		if (val == null)
			return null;

		try {
			return Boolean.valueOf(val);
		} catch (Exception e) {
			throw new InvalidTypeException("This value is not a boolean.");
		}
	}

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @param def Valeur par défaut
	 * @return valeur de la clé, <code>def</code> si elle n'existe pas
	 * @throws net.samagames.api.player.InvalidTypeException si la valeur n'est pas du bon type
	 */
	public Boolean getBoolean(String key, boolean def) {
		Boolean ret = getBoolean(key);
		if (ret == null)
			return def;
		else
			return ret;
	}

	/**
	 * Définit une valeur dans les données du joueur
	 * @param key	Clé à définir
	 * @param value Valeur à définir
	 */
	public abstract void setBoolean(String key, boolean value);

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @return valeur de la clé, null si elle n'existe pas
	 * @throws net.samagames.api.player.InvalidTypeException si la valeur n'est pas du bon type
	 */
	public Double getDouble(String key) {
		String val = get(key);
		if (val == null)
			return null;

		try {
			return Double.valueOf(val);
		} catch (Exception e) {
			throw new InvalidTypeException("This value is not a double.");
		}
	}

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @param def Valeur par défaut
	 * @return valeur de la clé, <code>def</code> si elle n'existe pas
	 * @throws InvalidTypeException si la valeur n'est pas du bon type
	 */
	public Double getDouble(String key, double def) {
		Double ret = getDouble(key);
		if (ret == null)
			return def;
		else
			return ret;
	}

	/**
	 * Définit une valeur dans les données du joueur
	 * @param key	Clé à définir
	 * @param value Valeur à définir
	 */
	public abstract void setDouble(String key, double value);

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @return valeur de la clé, null si elle n'existe pas
	 * @throws net.samagames.api.player.InvalidTypeException si la valeur n'est pas du bon type
	 */
	public Long getLong(String key) {
		String val = get(key);
		if (val == null)
			return null;

		try {
			return Long.valueOf(val);
		} catch (Exception e) {
			throw new InvalidTypeException("This value is not a long.");
		}
	}

	/**
	 * Récupère la valeur d'une clé
	 * @param key clé à récupérer
	 * @param def Valeur par défaut
	 * @return valeur de la clé, <code>def</code> si elle n'existe pas
	 * @throws net.samagames.api.player.InvalidTypeException si la valeur n'est pas du bon type
	 */
	public Long getLong(String key, long def) {
		Long ret = getLong(key);
		if (ret == null)
			return def;
		else
			return ret;
	}

	/**
	 * Définit une valeur dans les données du joueur
	 * @param key	Clé à définir
	 * @param value Valeur à définir
	 */
	public abstract void setLong(String key, long value);
}
