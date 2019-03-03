public class hooman extends Thread implements hoomanCallback {
    String portName;
    Communicator cm;

    public hooman(String portName) {
        this.portName = portName;
        cm = new Communicator(this);
    }

    public static void main(String[] args) {
        Communicator cm = new Communicator();
        cm.searchForPorts();
        cm.portMap.forEach((o, o2) -> {
            System.out.println(o);
        });
        (new hooman("COM4")).start();
    }

    @Override
    public void run() {
        if (cm.connect(portName, 9600, 8, 1, 0)) {
            if (cm.initIOStream() == true) {
                cm.initListener();
            }
            while (cm.getConnected()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void processData(String data) {
        switch (data) {
            case "h":
                cm.writeString("Hooman\r\n");
                break;
            case "f":
                cm.writeString("Fateme\r\n");
                break;
            default:
                cm.writeString("WTF!\r\n");
                break;
        }
    }
}
