package catworld.seguridad;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import net.fabricmc.loader.api.FabricLoader;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class Auth {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("catworld_auth.json");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final int BCRYPT_COST = 12;

    private String uuid;

    private String email;

    private String password;

    private String ip;

    @SerializedName("last-session")
    private String lastSession;

    @SerializedName("registred")
    private String registered;

    private boolean banned;

    private String status;

    private int amnistia;

    public Auth() {
    }

    private Auth(String uuid, String email, String passwordHash, String ip) {
        this.uuid = uuid;
        this.email = email;
        this.password = passwordHash;
        this.ip = ip;
        this.registered = LocalDateTime.now().format(DATE_FORMAT);
        this.lastSession = this.registered;
        this.banned = false;
        this.status = "player";
        this.amnistia = 0;
    }

    // =====================  Getters / Setters =====================

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return password;
    }

    public void setPasswordHash(String passwordHash) {
        this.password = passwordHash;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLastSession() {
        return lastSession;
    }

    public void setLastSession(String lastSession) {
        this.lastSession = lastSession;
    }

    public String getRegistered() {
        return registered;
    }

    public void setRegistered(String registered) {
        this.registered = registered;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAmnistia() {
        return amnistia;
    }

    public void setAmnistia(int amnistia) {
        this.amnistia = amnistia;
    }

    public static synchronized boolean register(Map<String, Auth> authByUuid, String uuid, String email, String plainPassword, String ip) {
        if (authByUuid.containsKey(uuid)) {
            return false;
        }

        String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
        Auth auth = new Auth(uuid, email, hash, ip);
        authByUuid.put(uuid, auth);
        save(authByUuid);
        return true;
    }

    public static synchronized boolean login(Map<String, Auth> authByUuid, String uuid, String plainPassword, String ip) {
        Auth auth = authByUuid.get(uuid);
        if (auth == null || auth.isBanned()) {
            return false;
        }

        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), auth.getPasswordHash());
        if (!result.verified) {
            return false;
        }

        auth.setLastSession(LocalDateTime.now().format(DATE_FORMAT));
        auth.setIp(ip);
        save(authByUuid);
        return true;
    }

    public static Auth findByUuid(Map<String, Auth> authByUuid, String uuid) {
        if (uuid == null) {
            return null;
        }
        return authByUuid.get(uuid);
    }

    // =====================  JSON I/O =====================

    public static void loadAll(Map<String, Auth> authByUuid) {
        authByUuid.clear();

        if (!Files.exists(FILE_PATH)) {
            System.out.println("[Auth] " + FILE_PATH + " 404 error.");
            writeToDisk(authByUuid);
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (!root.isJsonArray()) {
                System.err.println("[Auth] " + FILE_PATH + " broken.");
                return;
            }

            for (JsonElement element : root.getAsJsonArray()) {
                Auth auth = GSON.fromJson(element, Auth.class);
                if (auth.getUuid() != null) {
                    authByUuid.put(auth.getUuid(), auth);
                }
            }

        } catch (IOException e) {
            System.err.println("[Auth] 504 error " + FILE_PATH);
            e.printStackTrace();
        }
    }

    public static synchronized void save(Map<String, Auth> authByUuid) {
        writeToDisk(authByUuid);
    }

    private static void writeToDisk(Map<String, Auth> authByUuid) {
        JsonArray array = new JsonArray();
        for (Auth auth : authByUuid.values()) {
            array.add(GSON.toJsonTree(auth));
        }

        try {
            Files.createDirectories(FILE_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(array, writer);
            }
        } catch (IOException e) {
            System.err.println("[Auth] Can't save " + FILE_PATH);
            e.printStackTrace();
        }
    }
}
