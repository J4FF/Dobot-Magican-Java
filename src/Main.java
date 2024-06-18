import CPlusDll.DobotDll;
import CPlusDll.DobotDll.*;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;


public class Main {
    public static void main(String[] args) {
        try {
            Main app = new Main();
            app.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Programmstart");
        if (connectToDobot()) {
            configureDobot();

            // Beispielhafte Bewegung zu Koordinaten
            activateSuction(true);
            moveDobotToPosition(200, 180, 28, 100);
            moveDobotToPosition(195, 178, -23, 52);
            moveDobotToPosition(195, 179, 58, 60);
            moveDobotToPosition(200, 180, 28, 20);


            try {
                // Implementiere eine geeignete Logik, um auf die Fertigstellung der Bewegung zu warten
                Thread.sleep(2000); // Wartezeit als Platzhalter
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Bewegung zu Positionen abgeschlossen.");
            SetEndEffectorSuctionCup();
        }
    }

    private boolean connectToDobot() {
        DobotResult ret = DobotResult.values()[DobotDll.instance.ConnectDobot((char) 0, 115200, (char) 0, (char) 0)];
        if (ret == DobotResult.DobotConnect_NoError) {
            System.out.println("Verbindung erfolgreich: " + ret.name());
            return true;
        } else {
            System.out.println("Verbindungsfehler: " + ret.name());
            return false;
        }
    }
    private static void setGripperOff() {
        boolean enableCtrl = false; // Endeffektor-Steuerung deaktivieren
        boolean grip = false; // Greifer ausschalten
        boolean isQueued = false; // Direkte Ausführung, nicht in die Warteschlange stellen
        long[] queuedCmdIndex = new long[1]; // Befehl-Index (nur relevant, wenn isQueued = true)

        int result = DobotControl.DobotDll.INSTANCE.SetEndEffectorGripper(enableCtrl, grip, isQueued, queuedCmdIndex);

        if (result != 0) {
            System.err.println("Fehler beim Ausschalten des Greifers!");
        }
    }
    private void configureDobot() {
        System.out.println("Dobot konfigurieren...");
        IntByReference ib = new IntByReference();
        // Weitere Konfigurationsschritte folgen...

        // Setze die Parameter für den Endeffektor
        EndEffectorParams endEffectorParams = new EndEffectorParams();
        endEffectorParams.xBias = 71.6f;
        endEffectorParams.yBias = 0;
        endEffectorParams.zBias = 0;
        DobotDll.instance.SetEndEffectorParams(endEffectorParams, false, ib);

        // Setze JOG-Gelenkparameter
        JOGJointParams jogJointParams = new JOGJointParams();
        for (int i = 0; i < 4; i++) {
            jogJointParams.velocity[i] = 200; // Geschwindigkeit
            jogJointParams.acceleration[i] = 200; // Beschleunigung
        }
        DobotDll.instance.SetJOGJointParams(jogJointParams, false, ib);

        // Setze JOG-Koordinatenparameter
        JOGCoordinateParams jogCoordinateParams = new JOGCoordinateParams();
        for (int i = 0; i < 4; i++) {
            jogCoordinateParams.velocity[i] = 200; // Geschwindigkeit
            jogCoordinateParams.acceleration[i] = 200; // Beschleunigung
        }
        DobotDll.instance.SetJOGCoordinateParams(jogCoordinateParams, false, ib);

        // Setze JOG-Gemeinsame Parameter
        JOGCommonParams jogCommonParams = new JOGCommonParams();
        jogCommonParams.velocityRatio = 50; // Geschwindigkeitsverhältnis
        jogCommonParams.accelerationRatio = 50; // Beschleunigungsverhältnis
        DobotDll.instance.SetJOGCommonParams(jogCommonParams, false, ib);

        // Setze PTP-Gelenkparameter
        PTPJointParams ptpJointParams = new PTPJointParams();
        for (int i = 0; i < 4; i++) {
            ptpJointParams.velocity[i] = 200; // Geschwindigkeit
            ptpJointParams.acceleration[i] = 200; // Beschleunigung
        }
        DobotDll.instance.SetPTPJointParams(ptpJointParams, false, ib);

        // Setze PTP-Koordinatenparameter
        PTPCoordinateParams ptpCoordinateParams = new PTPCoordinateParams();
        ptpCoordinateParams.xyzVelocity = 200; // X, Y, Z Geschwindigkeit
        ptpCoordinateParams.xyzAcceleration = 200; // X, Y, Z Beschleunigung
        ptpCoordinateParams.rVelocity = 200; // Rotationsgeschwindigkeit
        ptpCoordinateParams.rAcceleration = 200; // Rotationsbeschleunigung
        DobotDll.instance.SetPTPCoordinateParams(ptpCoordinateParams, false, ib);

        // Setze PTP-Sprungparameter
        PTPJumpParams ptpJumpParams = new PTPJumpParams();
        ptpJumpParams.jumpHeight = 20; // Sprunghöhe
        ptpJumpParams.zLimit = 180; // Maximale Sprunghöhe
        DobotDll.instance.SetPTPJumpParams(ptpJumpParams, false, ib);

        // Bereite den Befehlswarteschlange vor
        DobotDll.instance.SetCmdTimeout(3000);
        DobotDll.instance.SetQueuedCmdClear();
        DobotDll.instance.SetQueuedCmdStartExec();
    }

    private void moveDobotToPosition(float x, float y, float z, float r) {
        IntByReference ib = new IntByReference();
        PTPCmd cmd = new PTPCmd();
        cmd.ptpMode = (byte) PTPMode.PTPMOVJXYZMode.ordinal();
        cmd.x = x;
        cmd.y = y;
        cmd.z = z;
        cmd.r = r;

        System.out.printf("Bewege zu Koordinaten (%.2f, %.2f, %.2f, %.2f)\n", x, y, z, r);
        DobotDll.instance.SetPTPCmd(cmd, true, ib);
        waitForMovementToComplete();
    }

    private void waitForMovementToComplete() {
        try {
            // Implementiere eine geeignete Logik, um auf die Fertigstellung der Bewegung zu warten
            Thread.sleep(1000); // Wartezeit als Platzhalter
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void activateSuction(boolean enable) {
        boolean enableCtrl = true; // Endeffektor-Steuerung aktivieren
        boolean grip = enable; // Greifer schließen/öffnen
        boolean isQueued = false; // Direkte Ausführung, nicht in die Warteschlange stellen
        long[] queuedCmdIndex = new long[1]; // Befehl-Index (nur relevant, wenn isQueued = true)

        int result = DobotControl.DobotDll.INSTANCE.SetEndEffectorGripper(enableCtrl, grip, isQueued, queuedCmdIndex);

        if (result != 0) {
            System.err.println("Fehler beim Setzen des Greifers!");
        }
    }

    private static void SetEndEffectorSuctionCup() {
        boolean enableCtrl = false; // Endeffektor-Steuerung deaktivieren
        boolean suck = false; // Greifer ausschalten
        boolean isQueued = false; // Direkte Ausführung, nicht in die Warteschlange stellen
        long[] queuedCmdIndex = new long[1]; // Befehl-Index (nur relevant, wenn isQueued = true)

        int result = DobotControl.DobotDll.INSTANCE.SetEndEffectorSuctionCup(false, false, false, queuedCmdIndex);

        if (result != 0) {
            System.err.println("Fehler beim Ausschalten des Greifers!");
        }
    }

    public class DobotControl {
        // Interface zur Dobot DLL
        public interface DobotDll extends com.sun.jna.Library {
            DobotDll INSTANCE = (DobotDll) Native.loadLibrary("DobotDll", DobotDll.class);

            int SetEndEffectorGripper(boolean enableCtrl, boolean grip, boolean isQueued, long[] queuedCmdIndex);
            int SetEndEffectorSuctionCup(boolean enableCtrl, boolean suck, boolean isQueued, long[] queuedCmdIndex);


        }
    }
}

