import java.util.ArrayList;
import java.util.List;

/**
 * Created by thebaker on 3/10/17.
 */
public class Main {

    public static void main(String[] args) {
        new Main();
//        Main m = new Main();
//        System.out.println(m.hexStringtoDecimal("0x10"));
//        System.out.println("64: "+m.decimaltoHexString(64));
    }

    Main() {
        start();
    }

    private void start() {
        //initialize iv array
        //initialize message array
        blockByBlock();

    }

    private int hexStringtoDecimal(String iv) {
        return Integer.decode(iv);
    }

    private String decimaltoHexString(int mask) {
        String temp = Integer.toHexString(mask);
        return temp.length() == 1 ? "0" + temp : temp;
    }

    private void blockByBlock() {
        String modIV = "";
        String iv;
        String msg;
//        for (int i = 1; i < Constants.ENCRYPTED_TEXT.length-1; i++) {
//            iv = Constants.ENCRYPTED_TEXT[i - 1];
//            msg = Constants.ENCRYPTED_TEXT[i];
//            modIV = byteByByte(iv, msg);
//            System.out.println("Mod IV : " + modIV);
//            System.out.println("IV     : " + iv);
//            System.out.println("Message: " + msg);
//            System.out.println(convertHexToString(finalModify(modIV),iv));
//        }
        specialCase();

    }

    private String byteByByte(String iv, String msg) {
        String modIV = "";
        for (int i = 32; i > 0; i -= 2) {//byte by byte for-loop starting from the right
            for (int j = 0; j < 256; j++) {//find the proper R value
                String tempIV = iv.substring(0, i - 2) + decimaltoHexString(j) + padModify(modIV);
//                System.out.println("IV  : " + iv);
//                System.out.println("Temp: " + tempIV);
                if (CommandLine.client(tempIV + msg)) {
//                    System.out.println("Adding this to modIV: " + decimaltoHexString(j));
                    modIV = decimaltoHexString(j) + modIV;
                    break;
                }
            }
        }
        return modIV;
    }

    /**
     * for the last block of encoding
     */
    private void specialCase() {
        int length = Constants.ENCRYPTED_TEXT.length;
        String iv = Constants.ENCRYPTED_TEXT[length - 2];
        String msg = Constants.ENCRYPTED_TEXT[length - 1];
        String modIV = "";
        List<String> possible = new ArrayList<>();
        for (int i = 32; i > 0; i -= 2) {//byte by byte for-loop starting from the right
            String currentByte = iv.substring(i-2, i);
            for (int j = 0; j < 256; j++) {//find the proper R value
                String tempIV = iv.substring(0, i - 2) + decimaltoHexString(j) + padModify(modIV);
                if (CommandLine.client(tempIV + msg)) {
                    possible.add(decimaltoHexString(j));
                }
            }
            if(possible.size()>1) modIV = ((possible.get(0).equals(currentByte))?possible.get(1):possible.get(0)) + modIV;
            else modIV = possible.get(0)+modIV;
            possible.clear();
        }


        System.out.println("Mod IV : " + modIV);
        System.out.println("IV     : " + iv);
        System.out.println("Message: " + msg);
        System.out.println(convertHexToString(finalModify(modIV), iv));
    }

    /**
     * Takes in the String of my modifications and change them so that they suit the current padding
     * i.e. modIV = 0x4a5d
     * The padding = modIV.length()/2 + 1 = 2 + 1 = 3. For the next byte to be modified
     * The first byte will be 0x5d. This byte was modified by me to obtain a padding of 0x01.
     * To change this modified 0x5d to obtain a padding of 0x03 = 0x01 ^ (?)
     * (?) = 0x01 ^ 0x03 = 0x02
     * Therefore: 0x5d ^ 0x02 = 0x5f
     * The second byte will be 0x4a. This byte was modified by me to obtain a padding of 0x02.
     * To change this modified 0x4a to obtain a padding of 0x03 = 0x02 ^ (?)
     * (?) = 0x02 ^ 0x03 = 0x01
     * Therefore: 0x4a ^ 0x01 = 0x4b
     * To generalize: new byte = old byte ^ its padding ^ target padding
     *
     * @param modIV
     * @return A modIV that is appropiate for the next padding target
     */
    private String padModify(String modIV) {
        if (modIV.isEmpty()) return "";
        String padIV = "";
        int targetPad = modIV.length() / 2 + 1;
        for (int i = modIV.length(), pad = 1; i > 0; i -= 2, pad++) {
            int tempByte = hexStringtoDecimal(Constants.HEX + modIV.substring(i - 2, i));
            tempByte ^= pad ^ targetPad;
            padIV = decimaltoHexString(tempByte) + padIV;
        }
        return padIV;
    }

    private String finalModify(String modIV) {
        String padIV = "";
        for (int i = modIV.length(), pad = 1; i > 0; i -= 2, pad++) {
            int tempByte = hexStringtoDecimal(Constants.HEX + modIV.substring(i - 2, i));
            tempByte ^= pad;
            padIV = decimaltoHexString(tempByte) + padIV;
        }
        return padIV;
    }

    /**
     * copied from here:
     * https://www.mkyong.com/java/how-to-convert-hex-to-ascii-in-java/
     *
     * @param modIV
     * @return
     */
    public String convertHexToString(String modIV, String encoded) {

        StringBuilder sb = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < modIV.length() - 1; i += 2) {

            //grab the hex in pairs
            String mod = modIV.substring(i, (i + 2));
            String enc = encoded.substring(i, i + 2);
            //convert hex to decimal
            int modDec = Integer.parseInt(mod, 16);
            int encDec = Integer.parseInt(enc, 16);
            //convert the decimal to character
            String temp = "";
            switch ((char) (modDec ^ encDec))
            {
                case 1:
                    temp+="0x01";
                    break;
                case 2:
                    temp+="0x02";
                    break;
                case 3:
                    temp+="0x03";
                    break;
                case 4:
                    temp+="0x04";
                    break;
                case 5:
                    temp+="0x05";
                    break;
                case 6:
                    temp+="0x06";
                    break;
                case 7:
                    temp+="0x07";
                    break;
                case 8:
                    temp+="0x08";
                    break;
//                case 9:
//                    temp+="0x01";
//                    break;
//                case 10:
//                    temp+="0x01";
//                    break;
                case 11:
                    temp+="0x0B";
                    break;
                case 12:
                    temp+="0x0C";
                    break;
                case 13:
                    temp+="0x0D";
                    break;
                case 14:
                    temp+="0x0E";
                    break;
                case 15:
                    temp+="0x0F";
                    break;
                case 16:
                    temp+="0x10";
                    break;
                case '\t':
                    temp = "\\t";
                    break;
                case '\n':
                    temp = "\\n";
                    break;
                default:
                    temp += (char) (modDec ^ encDec);
                    break;
            }

            sb.append(temp);

        }
        return sb.toString();
    }
}