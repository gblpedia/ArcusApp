package au.com.adilamtech.app.arcus;

public interface ArcusConnectionEventListener {

   void onCompletion();
   void onException(Exception e);
}
