package com.necatitufan.sharpbenddetection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.ArrayList;

import android.location.LocationListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Geo;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.geo.Projection;
import com.yandex.mapkit.geometry.geo.Projections;
import com.yandex.mapkit.geometry.geo.XYPoint;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.Session;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.mapkit.search.SuggestOptions;
import com.yandex.mapkit.search.SuggestSession;
import com.yandex.mapkit.search.SuggestType;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

/**
 * This example shows how to build routes between two points and display them on the map.
 * Note: Routing API calls count towards MapKit daily usage limits. Learn more at
 * https://tech.yandex.ru/mapkit/doc/3.x/concepts/conditions-docpage/#conditions__limits
 */
public class MainActivity extends Activity implements DrivingSession.DrivingRouteListener, CameraListener, LocationListener, View.OnClickListener, Session.SearchListener, TextView.OnEditorActionListener
{
    // API anahtarı
    // API key
    private final String MAPKIT_API_KEY = "94063153-9bdf-49ea-869f-e6f60bd65357";
    // Rota başlangıç konumu (Uşak/Merkez)
    // Default route startup location (Uşak/Merkez, Türkiye)
    private Point ROUTE_START_LOCATION = new Point(38.674137, 29.405815);
    // Rota bitiş konumu (Manisa/Alaşehir)
    // Default route finish location (Manisa/Alaşehir, Türkiye)
    private Point ROUTE_FINISH_LOCATION = new Point(38.360319, 28.515394);

    // Haritada görünen merkez başlangıç noktası olacaktır.
    // The centre shown on the map will be the starting point.
    private Point SCREEN_CENTER = ROUTE_START_LOCATION;

    // Harita görüntüleme bileşeni
    // Map display component
    private MapView mapView;
    // Harita üzerindeki çizim nesnelerini barındıran nesne
    // Object that contains drawing objects on the map
    private MapObjectCollection mapObjects;
    // Rotaları hesaplayan ve oluşturan nesne
    // Object that calculates and generates routes
    private DrivingRouter drivingRouter;
    // Seçilen rota bilgisini barındıran nesne
    // Object containing the selected route information
    private DrivingRoute drivingRoute;
    // Mevcut konumu gösteren simge
    // Icon showing the current position
    private PlacemarkMapObject locationIcon;
    // Rota çizgisini gösteren harita nesnesi
    // Map object showing the route line
    private PolylineMapObject polylineMapObject;
    // Rota içindeki segment noktalarının başlangıç sırası
    // Starting order of segment points within the route
    private int segmentCount = 0;
    // Arama yöneticisi
    // Search manager
    private SearchManager searchManager;
    // Öneri sonuçları
    // Suggest session
    private SuggestSession suggestSession;
    private final SuggestOptions SEARCH_OPTIONS = new SuggestOptions().setSuggestTypes(
            SuggestType.GEO.value | SuggestType.BIZ.value | SuggestType.TRANSIT.value);
    private final double BOX_SIZE = 0.2;
    private final BoundingBox BOUNDING_BOX = new BoundingBox(
            new Point(ROUTE_START_LOCATION.getLatitude() - BOX_SIZE, ROUTE_START_LOCATION.getLongitude() - BOX_SIZE),
            new Point(ROUTE_START_LOCATION.getLatitude() + BOX_SIZE, ROUTE_START_LOCATION.getLongitude() + BOX_SIZE));
    private final List<String> suggestResultA = new ArrayList<>();
    private final List<String> suggestResultB = new ArrayList<>();
    private ArrayAdapter<String> adapterLocationA;
    private ArrayAdapter<String> adapterLocationB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Mapkit tanımlamaları
        // Mapkit definitions
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);
        SearchFactory.initialize(this);

        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        // Ara yüzde tanımlanan butonların tıklama olaylarının tanımlanması
        // Defining the click events of the buttons defined on the interface
        Button btnStartSimulation = findViewById(R.id.btnStartSimulation);
        btnStartSimulation.setOnClickListener(this);
        Button btnStopSimulation = findViewById(R.id.btnStopSimulation);
        btnStopSimulation.setOnClickListener(this);

        // Harita kütüphanesindeki ayarlamalar
        // Mapkit library settings
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.getMap().move(new CameraPosition(SCREEN_CENTER, 19.0f, 180f, 60.0f));
        mapView.getMap().addCameraListener(this);
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        suggestSession = searchManager.createSuggestSession();
        AutoCompleteTextView etLocationA = findViewById(R.id.etLocationA);
        AutoCompleteTextView etLocationB = findViewById(R.id.etLocationB);
        Button btnCreateRoute = findViewById(R.id.btnCreateRoute);
        etLocationA.setOnEditorActionListener(this);
        etLocationB.setOnEditorActionListener(this);
        btnCreateRoute.setOnClickListener(this);
        suggestResultA.add("Uşak");
        adapterLocationA = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestResultA);
        adapterLocationB = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestResultB);

        etLocationA.setAdapter(adapterLocationA);
        etLocationA.setThreshold(1);
        etLocationB.setAdapter(adapterLocationB);

        etLocationA.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                suggestSession.suggest(charSequence.toString(), BOUNDING_BOX, SEARCH_OPTIONS, new SuggestSession.SuggestListener()
                {
                    @Override
                    public void onResponse(@NonNull List<SuggestItem> list)
                    {
                        adapterLocationA.clear();
                        suggestResultA.clear();
                        for (int i = 0; i < Math.min(5, list.size()); i++)
                        {
                            suggestResultA.add(list.get(i).getDisplayText());
                        }
                        adapterLocationA.addAll(suggestResultA);
                        adapterLocationA.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(@NonNull Error error)
                    {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                adapterLocationA.notifyDataSetChanged();
            }
        });

        etLocationB.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                suggestSession.suggest(charSequence.toString(), BOUNDING_BOX, SEARCH_OPTIONS, new SuggestSession.SuggestListener()
                {
                    @Override
                    public void onResponse(@NonNull List<SuggestItem> list)
                    {
                        adapterLocationB.clear();
                        suggestResultB.clear();
                        for (int i = 0; i < Math.min(5, list.size()); i++)
                            suggestResultB.add(list.get(i).getDisplayText());

                        adapterLocationB.addAll(suggestResultB);
                        adapterLocationB.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(@NonNull Error error)
                    {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                adapterLocationB.notifyDataSetChanged();
            }
        });

        // Cihazın konum bilgilerini kullanabilmek için izinleri al
        // Obtain permissions to use the device's location information
        getLocationPermissions();

        // Başangıç ve bitiş noktalarına göre kütüphaneden rota talep et
        // Request a route from the library according to the start and end points
        requestRoute();

        // Konum simgesi ekleme
        // Adding a location icon
        locationIcon = mapObjects.addPlacemark(ROUTE_START_LOCATION, ImageProvider.fromResource(this, R.drawable.ic_arrow));
    }

    private void getLocationPermissions()
    {
        // Konum izni alınmamış mı kontrol et
        // Check if the location is not authorised
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            // İzinler alınmamışsa kullanıcıdan izin talep et
            // Request permission from the user if permissions have not been obtained
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    0);
        }
        else
        {
            // İzinler alınmışsa konum değişimlerini her 1 saniyede alabilmek için olay tanımla
            // If permissions are obtained, define an event to receive location changes every 1 second
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }
    }

    @Override
    protected void onStop()
    {
        // onStop calls should be passed to MapView and MapKit instances.
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart()
    {
        // onStart calls should be passed to MapView and MapKit instances.
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public void onDrivingRoutes(List<DrivingRoute> rotalar)
    {
        // Oluşturulan en iyi rota keskin virajları bulmak için analiz edilir
        // The best route generated is analysed to find sharp bends
        findSharpBends(rotalar.get(0));
    }

    // Belirtilen rotaya göre keskin virajları bulur
    // Finds sharp bends according to the specified route
    private void findSharpBends(DrivingRoute route)
    {
        // Daha önceden tanımlı rota çizgisi varsa sil
        // Delete if there is a predefined route line
        if (polylineMapObject != null)
            mapObjects.remove(polylineMapObject);

        // Harita üzerinde rota çizgilerini gösterme
        // Show route lines on the map
        polylineMapObject = mapObjects.addPolyline(route.getGeometry());

        // Sahte konum ayarlamasında kullanılmak üzere "route", "drivingRoute" değişkenine aktarılır
        // The variable "route" is passed to the variable "drivingRoute" for use in the false position setting
        drivingRoute = route;

        // Rotadaki ara noktaların listesini al
        // Get a list of waypoints on the route
        List<Point> points = route.getGeometry().getPoints();

        // Tüm ara noktaları keskin viraj için değerlendir
        // Evaluate all intermediate points for sharp cornering
        for (int i = 0; i < points.size() - 20; i++)
        {
            // Başlangıç noktası (A)
            // Starting point (A)
            Point p1 = points.get(i);

            // Yaklaşık 50 metre sonraki orta ara noktanın tespiti işlemleri
            // Detection of the middle intermediate point approximately 50 metres later
            double mesafe1 = 0;
            int m1_sayac = 1;
            while (mesafe1 < 100)
            {
                mesafe1 += Geo.distance(p1, points.get(i + m1_sayac));
                m1_sayac++;
            }
            // Orta nokta (B)
            // Centre point (B)
            Point p2 = points.get(i + m1_sayac);

            // Yaklaşık 50 metre sonraki son ara noktanın tespiti işlemleri
            // Detection of the last waypoint in approximately 50 metres
            double mesafe2 = 0;
            int m2_sayac = m1_sayac + 1;
            while (mesafe2 < 100)
            {
                mesafe2 += Geo.distance(p2, points.get(i + m2_sayac));
                m2_sayac++;
            }
            // Bitiş noktası (C)
            // End point (C)
            Point p3 = points.get(i + m2_sayac);

            mapObjects.addPlacemark(p1, ImageProvider.fromResource(this, R.drawable.sn));

            // Konum değerlerini XY koordinat sistmeine dönüştür
            // Convert position values to XY coordinate system
            Projection projection = Projections.getSphericalMercator();
            XYPoint xy1 = projection.worldToXY(p1, 1);
            XYPoint xy2 = projection.worldToXY(p2, 1);
            XYPoint xy3 = projection.worldToXY(p3, 1);

            // Sin(a) ve Cos(a) hesapla
            // Calculate Sin(a) and Cos(a)
            double a = xy2.getY() - xy1.getY();
            double b = xy2.getX() - xy1.getX();
            double sin_a = Math.abs(a) / Math.sqrt((a * a) + (b * b));
            double cos_a = Math.abs(b) / Math.sqrt((a * a) + (b * b));

            // Sin(b) ve Cos(b) hesapla
            // Calculate Sin(b) and Cos(b)
            double c = xy3.getY() - xy2.getY();
            double d = xy3.getX() - xy2.getX();
            double sin_b = Math.abs(c) / (Math.sqrt((c * c) + (d * d)));
            double cos_b = Math.abs(d) / (Math.sqrt((c * c) + (d * d)));

            // Sin(a+b)
            double sin_ab = (sin_a * cos_b) + (cos_a * sin_b);
            // Cos(a+b)
            double cos_ab = (cos_a * cos_b) - (sin_a * sin_b);

            // A ve B' noktası arasındaki y eksenindeki uzunluk (Aynı zamanda yükseklik)
            // Length on the y-axis between points A and B' (also height)
            double h = sin_ab * Math.sqrt((a * a) + (b * b));
            // A ve B' noktası arasındaki x eksenindeki uzunluk
            // Length on the x-axis between points A and B'
            double x = cos_ab * Math.sqrt((a * a) + (b * b));

            // B' noktasının koordinatlarının oluşturulması
            // Establishing the coordinates of point 'B'
            XYPoint xy2_new = new XYPoint(xy1.getX() + x, xy1.getY() + h);

            // Eğimin hesaplanması
            // Calculating the slope
            double slope = Math.abs(xy2_new.getY() - xy1.getY()) / Math.abs(xy2_new.getX() - xy1.getX());

            // Eğer eğim 5'den büyükse haritada ilgili noktaya kırmızı nokta yerleştir
            // Yapılan testler sonucunda keskin virajları tespit etmede eğimin
            // 5'den büyük olması durumunda en iyi sonuç alınmıştır.
            // If the slope is greater than 5, place a red dot at the corresponding point on the map
            // As a result of the tests, the best result was obtained when the slope was greater than 5 in detecting sharp bends.
            if (slope > 5.0)
            {
                mapObjects.addPlacemark(p2, ImageProvider.fromResource(this, R.drawable.ic_red_circle));
                i += m2_sayac;
            }

            // A ve C' noktası arasındaki x ekseninde olan uzaklık
            // The distance on the x-axis between point A and point C'
            double c3 = Math.sqrt((a * a) + (b * b));

            // C' noktasının koordinatlarının oluşturulması
            // Establishing the coordinates of point 'C'
            XYPoint xy3_new = new XYPoint(xy1.getX() + c3, xy1.getY());
        }
    }

    @Override
    public void onDrivingRoutesError(Error error)
    {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError)
        {
            errorMessage = getString(R.string.remote_error_message);
        }
        else if (error instanceof NetworkError)
        {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void requestRoute()
    {
        DrivingOptions options = new DrivingOptions();
        options.setAvoidTolls(false); // Ücretli yollar kapalı // Toll roads closed
        options.setRoutesCount(1); // En iyi rotayı seç // Choose the best route

        ArrayList<RequestPoint> a_b_points = new ArrayList<>();
        a_b_points.add(new RequestPoint(
                ROUTE_START_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        a_b_points.add(new RequestPoint(
                ROUTE_FINISH_LOCATION,
                RequestPointType.WAYPOINT,
                null));

        // Rota talep et ve rota olay fonksiyonu için bu sınıfı kullan
        // Request route and use this class for route event function
        drivingRouter.requestRoutes(a_b_points, options, new VehicleOptions(), this);
    }

    @Override
    public void onClick(View v)
    {
        // Simülasyonu başlat
        // Start simulation
        if (v.getId() == R.id.btnStartSimulation)
        {
            startMockLocations();
        }
        // Simülasyonu durdur
        // Stop simulation
        else if (v.getId() == R.id.btnStopSimulation)
        {
            mHandler.removeCallbacks(runnable);
        }
        else if (v.getId() == R.id.btnCreateRoute)
        {
            requestRoute();
        }
    }

    private Handler mHandler = new Handler();

    private void startMockLocations()
    {
        mHandler.removeCallbacks(runnable);
        mHandler.postDelayed(runnable, 1000);
    }

    private Runnable runnable = new Runnable()
    {
        public void run()
        {
            setMockLocations();
            mHandler.postDelayed(this, 1000);
        }
    };

    private void setMockLocations()
    {
        // Yeni sahte konum değerini rotadan al
        // Get the new dummy location value from the route
        Point pointCurrent = drivingRoute.getGeometry().getPoints().get(segmentCount);

        // Yeni konum değerini oluştur
        // Create new location value
        android.location.Location location = new android.location.Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(pointCurrent.getLatitude());
        location.setLongitude(pointCurrent.getLongitude());
        location.setAccuracy(0f);
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        location.setTime(System.currentTimeMillis());

        // Konum servisine yeni konumu ver
        // Give the new location to the location service
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, false, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE);
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);


        if (segmentCount != 0)
        {
            // Harita merkezini yeni konuma göre ayarla
            // Adjust map centre to new location
            SCREEN_CENTER = new Point(location.getLatitude(), location.getLongitude());

            Point pointPrev = drivingRoute.getGeometry().getPoints().get(segmentCount - 1);
            double azimuth = calculateAzimuth(pointPrev, pointCurrent);
            mapView.getMap().move(new CameraPosition(SCREEN_CENTER, 19.0f, (float) azimuth, 60.0f));
        }

        // Yeni konuma geçmesi için segment sayısını 1 arttır
        // Increase the number of segments by 1 to move to the new position
        if (segmentCount < drivingRoute.getGeometry().getPoints().size())
            segmentCount++;
        else
            segmentCount = 0;
    }

    // Simülasyonda kamera dönüş açısının derece cinsinden hesaplanması
    // Calculation of the camera rotation angle in degrees in the simulation
    private double calculateAzimuth(Point pointStart, Point pointFinish)
    {
        double latitude1 = Math.toRadians(pointStart.getLatitude());
        double latitude2 = Math.toRadians(pointFinish.getLatitude());
        double longDiff = Math.toRadians(pointFinish.getLongitude() - pointStart.getLongitude());
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    @Override
    public void onLocationChanged(android.location.Location location)
    {
        // Eski ok simgesini kaldır
        // Remove the old arrow icon
        mapObjects.remove(locationIcon);
        // Yeni konum değerine göre ok simgesini yerleştir
        // Place the arrow icon according to the new position value
        locationIcon = mapObjects.addPlacemark(new Point(location.getLatitude(), location.getLongitude()), ImageProvider.fromResource(this, R.drawable.ic_arrow));
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status,
                                Bundle extras)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSearchResponse(@NonNull Response response)
    {

    }

    @Override
    public void onSearchError(@NonNull Error error)
    {

    }

    // Arama kutusunda aratılan yer isminin bulunması ve başlangıç-bitiş noktlarının ayarlanması
    // Finding the searched place name in the search box and setting the start and end points
    private void findLocation(String query, boolean startup)
    {
        SearchOptions so = new SearchOptions();
        so = so.setSuggestWords(true);
        so = so.setGeometry(true);

        Session searchSession = searchManager.submit(
                query,
                VisibleRegionUtils.toPolygon(mapView.getMap().getVisibleRegion()),
                so,
                new Session.SearchListener()
                {
                    @Override
                    public void onSearchResponse(@NonNull Response response)
                    {
                        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();

                        GeoObjectCollection.Item searchResult = response.getCollection().getChildren().get(0);
                        Point resultLocation = searchResult.getObj().getGeometry().get(0).getPoint();
                        if (resultLocation != null)
                        {
                            mapObjects.addPlacemark(
                                    resultLocation,
                                    ImageProvider.fromResource(getApplicationContext(),
                                            startup ? R.drawable.ic_location_a : R.drawable.ic_location_b));

                            if (startup)
                                ROUTE_START_LOCATION = resultLocation;
                            else
                                ROUTE_FINISH_LOCATION = resultLocation;
                        }
                    }


                    @Override
                    public void onSearchError(@NonNull Error error)
                    {

                    }
                });
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
    {
        if (actionId == EditorInfo.IME_ACTION_SEARCH)
        {
            boolean startup = textView.getId() == R.id.etLocationA;
            findLocation(textView.getText().toString(), startup);
        }

        return false;
    }

    @Override
    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition, @NonNull CameraUpdateReason cameraUpdateReason, boolean b)
    {

    }
}

