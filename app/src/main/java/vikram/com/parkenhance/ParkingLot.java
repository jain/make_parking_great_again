package vikram.com.parkenhance;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by vikram on 6/10/16.
 */
public class ParkingLot {

    public enum STATUS{
        FULL, EMPTY, RELATIVELY_FULL, RELATIVELY_EMPTY
    }
    private STATUS status;
    private LatLng loc;

    public ParkingLot(String stat, double latitude, double longitude){
        switch (stat){
            case ("full"):
                status = STATUS.FULL;
                break;
            case ("empty"):
                status = STATUS.EMPTY;
                break;
            case ("relatively_full"):
                status = STATUS.RELATIVELY_FULL;
                break;
            case ("relatively_empty"):
                status = STATUS.RELATIVELY_EMPTY;
                break;
        }
        loc = new LatLng(latitude, longitude);
    }
    public STATUS getStatus(){
        return status;
    }
    public LatLng getLoc(){
        return loc;
    }
}
