# What is this application?
This is a GUI utility program created by Carpathia(the member of Tohkatsu Tech) 
for Serial-Communication in CanSat.

**Tohkatsu Tech**, a.k.a "東葛飾高校理科部航空班" is 
the part of the science club in Tohkatsu High School in Japan.

I created this program to be able to understand more well what the mbed and the twe-lite in CanSat send.<br>
Needless to say, this program is only suitable for the mbed whose program is created by me for 2020, 2021 CanSat program.

# For the Members of Tohkatsu Tech
After choosing a serial port, what this program can do is;
- Shows the positions of α-side and β-side
- Sends basic operations to α-side or β-side
- Shows the parsed data from α-side or β-side

# Data Format (expected)
Sorry to say, the format is little ugly because one value uses 5 bytes.<br>
`ID(1 byte) Value(4 bytes)`<br>

|ID|Type|
|---|---|
|0x01|Unsigned Int(32bits)|
|0x02|Float|

# Compile in your computer
To compile this, you should add `lib` folder and put `jSerialComm-2.6.2.jar`.<br>
refer: https://fazecast.github.io/jSerialComm/