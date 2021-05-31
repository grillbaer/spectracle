# Spectracle – a Spectrometer UI for Computer Attached Cameras

### WORK IN PROGRESS!

Yet another spectroscope UI. This app samples the spectrum from the video stream of a computer attached camera with an
attached spectroscope. It has been inspired by the great work from Youtube channel Les' Lab and some other great
youtubers.

## Examples

### Blue Sky with Fraunhofer Lines

![Blue Sky with Fraunhofer Lines](samples/blue_sky_good_calibration.png)

### Ordinary Fluorescent Lamp

![Ordinary Fluorescent Lamp](samples/fluorescent_lamp_good_calibration.png)

### High Quality Fluorescent Daylight Lamp

![High Quality Fluorescent Daylight Lamp](samples/fluorescent_daylight_good_calibration.png)

### Desktop LED Lamp with Good Light

![Desktop LED Lamp with Good Light](samples/led_lamp_good_calibration.png)

### Retro LED Lamp

![Retro LED Lamp](samples/retro_led_good_calibration.png)

### Pinkish Plant LED Panel

![Pinkish Plant LED Panel](samples/plant_light_good_calibration.png)

### Daylight Through a Green Plant Leaf

A leaf of sorrel:

![Daylight Through Green Plant Leaf](samples/daylight_through_green_leaf.png)

## Current Features

* Colored spectrum graph view
* Camera image view
* Two-point wavelength calibration
* Sensitivity calibration by comparing a captured spectrum with a known reference spectrum of an incandescent lamp or a
  halogen lamp
* Known wavelengths display:
    * Fraunhofer lines of the sun spectrum
    * Wavelengths of fluorescent tubes
* Peak and dent detection with wavelength display
* Time averaging
* Peak hold, combinable with time averaging for peak decay
* Gaussian smoothing for noise reduction
* Manual exposure control
* Overexposure visualization
* Play/Stop
* Saving and loading of captured spectra as CSV

## Hardware

The setup uses

* a Paton Hawksley benchtop spectroscope
* an ELP 1080P USB web cam with OV2710 1/2.7" color CMOS sensor
* a lens with 16 mm focal length, without IR filter, ⌀ 12mm threading.

The setup is far from optimal. The spectroscope itself is great. However, a RGB camera causes some problems in a
spectrometer due to the inconsistent sensitivity over the spectrum. So, the camera shows some false peaks or dents in
the spectrum depending on light intensity for yellow colors around 580 nm, just at the transition between green and red
filter. Camera and lens are quite cheap. The 12 mm threading is wobbely, I don't know which side is missing precision.
The lens thread used with this camera is also almost too short to focus to infinity, so that some separate holder is
required to stabilize the setup. The imprecise USB plug of my received item lost connection every now and then at least
in my notebook. Soldering a new plug fixed this. But hey, that's what to expect for that price.

This is the 3D printed fixture I use at the moment:

* [Spectroscope Camera Fixture @ onshape.com](https://cad.onshape.com/documents/067b9cbbfe91eaae340bbdb7/w/bf45124c7a9fedfbfbcc2b60/e/39af0ef3fc2ecf463972dfda)
* [STL file](hardware/fixture-v4.stl)

![Spectroscope Camera Fixture Photo](hardware/fixture-photo.jpg)
