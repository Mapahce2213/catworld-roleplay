package catworld.ciudadania;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import net.fabricmc.loader.api.FabricLoader;

public class Ciudadanos {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("catworld_ciudadanos.json");

    private String name;
    private String apedido;
    private String nickname;
    private int edad;
    private String gender;
    private String passport;
    private String cedula;
    private Derechos derechos = new Derechos();
    private Trabajo trabajo = new Trabajo();
    private List<String> estate = new ArrayList<>();
    private List<String> cars = new ArrayList<>();

    public Ciudadanos() {
    }

    public Ciudadanos(String name, String apedido, String nickname, int edad, String passport, String cedula) {
        this.name = name;
        this.apedido = apedido;
        this.nickname = nickname;
        this.edad = edad;
        this.passport = passport;
        this.cedula = cedula;
    }

    // =====================  Getters / Setters =====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApedido() {
        return apedido;
    }

    public void setApedido(String apedido) {
        this.apedido = apedido;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public Derechos getDerechos() {
        return derechos;
    }

    public void setDerechos(Derechos derechos) {
        this.derechos = derechos;
    }

    public Trabajo getTrabajo() {
        return trabajo;
    }

    public void setTrabajo(Trabajo trabajo) {
        this.trabajo = trabajo;
    }

    public List<String> getEstate() {
        return estate;
    }

    public void setEstate(List<String> estate) {
        this.estate = estate;
    }

    public List<String> getCars() {
        return cars;
    }

    public void setCars(List<String> cars) {
        this.cars = cars;
    }


    public static class Derechos {
        private Manejar manejar = new Manejar();
        private Armas armas = new Armas();

        public Manejar getManejar() {
            return manejar;
        }

        public void setManejar(Manejar manejar) {
            this.manejar = manejar;
        }

        public Armas getArmas() {
            return armas;
        }

        public void setArmas(Armas armas) {
            this.armas = armas;
        }
    }

    public static class Manejar {
        private boolean autos;
        private boolean motos;
        private boolean aviones;
        private boolean train;

        public boolean isAutos() {
            return autos;
        }

        public void setAutos(boolean autos) {
            this.autos = autos;
        }

        public boolean isMotos() {
            return motos;
        }

        public void setMotos(boolean motos) {
            this.motos = motos;
        }

        public boolean isAviones() {
            return aviones;
        }

        public void setAviones(boolean aviones) {
            this.aviones = aviones;
        }

        public boolean isTrain() {
            return train;
        }

        public void setTrain(boolean train) {
            this.train = train;
        }
    }

    public static class Armas {
        @SerializedName("ak-47")
        private boolean ak47;
        private boolean pistola;
        private boolean machete;

        public boolean isAk47() {
            return ak47;
        }

        public void setAk47(boolean ak47) {
            this.ak47 = ak47;
        }

        public boolean isPistola() {
            return pistola;
        }

        public void setPistola(boolean pistola) {
            this.pistola = pistola;
        }

        public boolean isMachete() {
            return machete;
        }

        public void setMachete(boolean machete) {
            this.machete = machete;
        }
    }

    public static class Trabajo {
        private String fraccion;
        private int rango;
        private String title;

        public String getFraccion() {
            return fraccion;
        }

        public void setFraccion(String fraccion) {
            this.fraccion = fraccion;
        }

        public int getRango() {
            return rango;
        }

        public void setRango(int rango) {
            this.rango = rango;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    // =====================  JSON I/O =====================

    public static void loadAll(Map<String, Ciudadanos> ciudadanosByCedula) {
        ciudadanosByCedula.clear();

        if (!Files.exists(FILE_PATH)) {
            System.out.println("[Ciudadania] " + FILE_PATH + " 404 error.");
            writeToDisk(ciudadanosByCedula);
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (!root.isJsonArray()) {
                System.err.println("[Ciudadania] " + FILE_PATH + " broken.");
                return;
            }

            for (JsonElement element : root.getAsJsonArray()) {
                Ciudadanos ciudadano = GSON.fromJson(element, Ciudadanos.class);
                if (ciudadano.getCedula() != null) {
                    ciudadanosByCedula.put(ciudadano.getCedula(), ciudadano);
                }
            }

        } catch (IOException e) {
            System.err.println("[Ciudadania] 504 error " + FILE_PATH);
            e.printStackTrace();
        }
    }

    public static void save(Map<String, Ciudadanos> ciudadanosByCedula) {
        writeToDisk(ciudadanosByCedula);
    }

    public static Ciudadanos findByNickname(Map<String, Ciudadanos> ciudadanosByCedula, String nickname) {
        if (nickname == null) {
            return null;
        }
        for (Ciudadanos c : ciudadanosByCedula.values()) {
            if (nickname.equalsIgnoreCase(c.getNickname())) {
                return c;
            }
        }
        return null;
    }

    public static Ciudadanos findByPassport(Map<String, Ciudadanos> ciudadanosByCedula, String passport) {
        if (passport == null) {
            return null;
        }
        for (Ciudadanos c : ciudadanosByCedula.values()) {
            if (passport.equalsIgnoreCase(c.getPassport())) {
                return c;
            }
        }
        return null;
    }

    private static void writeToDisk(Map<String, Ciudadanos> ciudadanosByCedula) {
        JsonArray array = new JsonArray();
        for (Ciudadanos ciudadano : ciudadanosByCedula.values()) {
            array.add(GSON.toJsonTree(ciudadano));
        }

        try {
            Files.createDirectories(FILE_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(array, writer);
            }
        } catch (IOException e) {
            System.err.println("[Ciudadania] Can't save " + FILE_PATH);
            e.printStackTrace();
        }
    }
}
