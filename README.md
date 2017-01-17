# UnlockSlideView
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)[ ![Download](https://api.bintray.com/packages/alexlytvynenko/alexlytvynenko/UnlockSlideView/images/download.svg) ](https://bintray.com/alexlytvynenko/alexlytvynenko/UnlockSlideView/_latestVersion)

**UnlockSlideView** - An Android custom slide view for unlock action that supports full view customization

![UnlockSlideView](https://raw.githubusercontent.com/alexlytvynenko/UnlockSlideView/master/art/slow_speed.gif)
![UnlockSlideView](https://raw.githubusercontent.com/alexlytvynenko/UnlockSlideView/master/art/normal_speed.gif)
![UnlockSlideView](https://raw.githubusercontent.com/alexlytvynenko/UnlockSlideView/master/art/fast_speed.gif)

## Gradle

```java
dependencies {
	compile 'com.alexlytvynenko.unlockslideview:unlockslideview:1.1'
}
```

## Usage

* In XML layout:

```xml
 <com.alexlytvynenko.unlockslideview.UnlockSlideView
        xmlns:slideview="http://schemas.android.com/apk/res-auto"
        android:id="@+id/view"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_centerInParent="true"
        slideview:limitProgress="90"
        slideview:resetSpeed="slow"
        slideview:text="HELLO WORLD"
        slideview:textBold="true"
        slideview:textColor="@android:color/holo_blue_light"
        slideview:textSize="20sp"
        slideview:textGravity="centerOfThumb"
        slideview:thumbPadding="5dp" />
```
* All customizable attributes:

```xml
<declare-styleable name="UnlockSlideView">
        <attr name="resetSpeed" format="enum">
            <enum name="fast" value="3" />
            <enum name="normal" value="2" />
            <enum name="slow" value="1" />
        </attr>
        <attr name="slideBackground" format="reference" />
        <attr name="thumb" format="reference" />
        <attr name="thumb_width" format="dimension" />
        <attr name="thumb_height" format="dimension" />
        <attr name="thumbPadding" format="dimension" />
        <attr name="text" format="string" />
        <attr name="textBold" format="boolean" />
        <attr name="textSize" format="dimension" />
        <attr name="textColor" format="color"/>
        <attr name="textPadding" format="dimension" />
        <attr name="textGravity" format="enum">
            <enum name="centerOfThumb" value="3" />
            <enum name="centerInParent" value="2" />
            <enum name="none" value="1" />
        </attr>
        <attr name="limitProgress" format="integer" />
    </declare-styleable>
```

## Sample
* Clone the repository and check out the `app` module.
* Download an [example apk](https://raw.githubusercontent.com/alexlytvynenko/UnlockSlideView/master/UnlockSlideView.apk) to check it.

## Licence
Copyright 2017 Alexander Lytvynenko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
