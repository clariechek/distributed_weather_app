import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WeatherEntryTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    
    // Reassign stdout stream to new PrintStream 
    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    // Restore stdout stream to original state
    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("Set Id Test")
    void testSetId() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setId("3");
        assertEquals("3", weatherEntry.getId());
    }

    @Test
    @DisplayName("Set Name Test")
    void testSetName() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setName("Adelaide (West Terrace /  ngayirdapira)");
        assertEquals("Adelaide (West Terrace /  ngayirdapira)", weatherEntry.getName());
    }

    @Test
    @DisplayName("Set State Test")
    void testSetState() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setState("SA");
        assertEquals("SA", weatherEntry.getState());
    }

    @Test
    @DisplayName("Set Time Zone Test")
    void testSetTime_zone() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setTime_zone("CST");
        assertEquals("CST", weatherEntry.getTime_zone());
    }

    @Test
    @DisplayName("Set Latitude Test - Negative value")
    void testSetLat1() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setLat(-34.9);
        assertEquals(-34.9, weatherEntry.getLat());
    }

    @Test
    @DisplayName("Set Latitude Test - Positive value")
    void testSetLat2() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setLat(69.2);
        assertEquals(69.2, weatherEntry.getLat());
    }

    @Test
    @DisplayName("Set Longitude Test")
    void testSetLon() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setLon(138.6);
        assertEquals(138.6, weatherEntry.getLon());
    }

    @Test
    @DisplayName("Set Local Date Time Test")
    void testSetLocal_date_time() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setLocal_date_time("15/04:00pm");
        assertEquals("15/04:00pm", weatherEntry.getLocal_date_time());
    }

    @Test
    @DisplayName("Set Local Date Time Full Test")
    void testSetLocal_date_time_full() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setLocal_date_time_full("20230715160000");
        assertEquals("20230715160000", weatherEntry.getLocal_date_time_full());
    }

    @Test
    @DisplayName("Set Air Temperature Test")
    void testSetAir_temp() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setAir_temp(13.3);
        assertEquals(13.3, weatherEntry.getAir_temp());
    }

    @Test
    @DisplayName("Set Apparent Temperature Test")
    void testSetApparent_t() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setApparent_t(9.5);
        assertEquals(9.5, weatherEntry.getApparent_t());
    }

    @Test
    @DisplayName("Set Cloud Test")
    void testSetCloud() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setCloud("Partly cloudy");
        assertEquals("Partly cloudy", weatherEntry.getCloud());
    }

    @Test
    @DisplayName("Set Dew Point Test")
    void testSetDewpt() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setDewpt(5.7);
        assertEquals(5.7, weatherEntry.getDewpt());
    }

    @Test
    @DisplayName("Set Pressure Test")
    void testSetPress() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setPress(1023.9);
        assertEquals(1023.9, weatherEntry.getPress());
    }

    @Test
    @DisplayName("Set Relative Humidity Test")
    void testSetRel_hum() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setRel_hum(Long.parseLong("60"));
        assertEquals(60, weatherEntry.getRel_hum());
    }

    @Test
    @DisplayName("Set Wind Direction Test")
    void testSetWind_dir() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setWind_dir("S");
        assertEquals("S", weatherEntry.getWind_dir());
    }

    @Test
    @DisplayName("Set Wind Speed (km/h) Test")
    void testSetWind_spd_kmh() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setWind_spd_kmh(Long.parseLong("15"));
        assertEquals(15, weatherEntry.getWind_spd_kmh());
    }

    @Test
    @DisplayName("Set Wind Speed (kt) Test")
    void testSetWind_spd_kt() {
        WeatherEntry weatherEntry = new WeatherEntry();
        weatherEntry.setWind_spd_kt(Long.parseLong("8"));
        assertEquals(8, weatherEntry.getWind_spd_kt());
    }
}
