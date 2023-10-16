# Sharp Bend Detection
With the increasing population and increasing number of vehicles in our country, traffic accidents increase during the year. However, sad events such as death, injury and disability are experienced. Driver, passenger, pedestrian, road and vehicle factors are effective in the realisation of traffic accidents. Although the effect of the road factor has been decreasing in recent years with the newly built and expanded and renewed roads, the road condition and shape is still a factor in traffic accidents. Within the road factor, there may be sharp bends on the roads in some regions due to the unevenness of the terrain. Sharp bends are also an effective factor in the realisation of traffic accidents.

In the navigation software used today, sharp bends are not shown on the route. For this reason, drivers who do not know the road can have traffic accidents on sharp bends on the road or have difficulty driving on bends.

For these reasons, a mobile application that can detect sharp bends on the roads has been developed that can run on the Android operating system. At the point of sharp bend detection, slope calculations in the coordinate system have an important place in the creation of the algorithm. After the algorithm, the appropriate Application Development Kit (SDK: Software Development Kit) was selected for map display operations. The application will detect the sharp bends on the route and add red round landmarks to these points.

## Algorithm Creation
The idea that by calculating the slope in the coordinate system, it is possible to determine whether a certain part of the route will be a sharp curve or not. The algorithm is constructed as follows:

**Step-1:** Creation of the route between two specified locations by means of a map library.

**Step-2:** Taking the waypoints forming the route.

**Step-3:** Approximately every 100 metres, the beginning, approximately 50 metres and the last point of the route segment are taken as a criterion.

**Step-4:** Conversion of the three coordinates obtained to the values in the coordinate system.

**Step-5:** In order to evaluate the slope of the three points under the same conditions, the starting and end points must be parallel to the X axis. For this reason, the other two points will be rotated around the starting point until they are parallel to the X axis, provided that the starting point remains constant.

**Step-6:** Calculation of the slope according to the starting and centre point.

**Step-7:** Determination of the sharp bend according to whether the slope is below or above the determined value.

**Step-8:** If a sharp bend is detected, a red bookmark is added to this point.

In the algorithm, geometry rules were utilised for the rotation of the points in **Step-5**. For example, let the 3 points determined be as follows:

![ornek_3_nokta.png](screenshots%2Fornek_3_nokta.png)

* Finding the lengths a, b and c in a right triangle according to the Pythagorean relation.
* Calculating the new coordinate of point C according to the found length c. The X value of point C will be more than the X value of A by c length. The Y value of C will be the same as the Y value of A.

**`C(x, y) = (A(x) + c, A(y))`**

* The new three points after the rotation are shown below as points A, BI and CI.

* ![ucgenin_a_noktasina_gore_dondurulmesi.png](screenshots%2Fucgenin_a_noktasina_gore_dondurulmesi.png)

* Trigonometry was also used to find the coordinate of point 'B'. Sin(m), Cos(m), Sin(n), Cos(n) values were calculated and Sin(m+n) and Cos(m+n) values were found according to the following sum-difference formulae.

**`Sin(m+n) = Sin(m)∙Cos(n) + Cos(m)∙Sin(n)`**

**`Cos(m+n) = Cos(m)∙Cos(n) - Sin(m)∙Sin(n)`**


* |AD| and h lengths are calculated as follows:

**`|AD| = Cos(m+n)∙c`**

**`h=Sin(m+n)∙c`**


* The coordinate of point 'B' is formed as follows:

**`B'(x, y) = (A(x) + |AD|, A(y) + h)`**


* After the conversion process, m (slope) value is calculated. Assuming that the coordinates of point A are (x1, y1) and the coordinates of point B' are (x2, y2), the slope formula is as follows.

**`m = (y2 - y1) / (x2 - x1)`**

As a result of the samples studied, it was observed that the slope value was higher in sharper turns. This value also plays an important role in detecting the sharp curve. For example, in the two examples below, different slope values are obtained and it is seen that the one with a larger slope has a sharper turn.

![ornek_yol_haritasi1.png](screenshots%2Fornek_yol_haritasi1.png)
![ornek_yol_haritasi2.png](screenshots%2Fornek_yol_haritasi2.png)

## App Screenshots

![ss1.png](screenshots%2Fss1.png)
![ss2.png](screenshots%2Fss2.png)
![ss3.png](screenshots%2Fss3.png)
![ss4.png](screenshots%2Fss4.png)


## Licence
    MIT License
    
    Copyright (c) 2023 Necati TUFAN
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

