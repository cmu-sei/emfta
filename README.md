# EMFTA
(An FTA editor/visualizer that uses EMF)
                             Julien Delange <jdelange@andrew.cmu.edu>

##In a Nutshell
* Latest build : https://raw.githubusercontent.com/cmu-sei/emfta/master/edu.cmu.emfta.updatesite/
* Mars update site: https://raw.githubusercontent.com/cmu-sei/emfta/mars/edu.cmu.emfta.updatesite/
* Luna update site: https://raw.githubusercontent.com/cmu-sei/emfta/luna/edu.cmu.emfta.updatesite/

## What it is?
This is an EMF-based FTA editor/visualizer. You can edit the content
in our FTA within Eclipse using a convenient representation (table)
and visualize a graphical (diagram) version.

If you are not familiar with what is an FTA, I would recommend
[to start reading wikipedia](http://en.wikipedia.org/wiki/Fault_tree_analysis) - this
is a method required by safety analysis methods (such as ARP4761).


## Why did you program that?
Most Fault-Tree Analysis tools are commercial and few open-source projects
are commercial and/or platform-dependent. Having an eclipse project
is way more convenient to interface with other modeling projects, 
especially the [OSATE](https://github.com/osate) tools used in many
of my research projects (yes, I did not do it for the sake of science).

Also, another reason was to try [Sirius](https://www.eclipse.org/sirius/), it
seems a nice way to design diagrams easily using EMF-based models. So, I wanted
to see what I can do within two evenings while coding on my porch.


## How it works?
Pretty simple: you create a modeling project, then, a basic FTA model
(using the *New* -> *Examples* menu in Eclipse) and then, right click
on the model and choose the appropriate representation (table
or tree). Diagram and table examples are shown below (see examples).

## Requirements
* A machine with Linux or Windows 8
* Java 8
* Some time (or a lot if you are a newbie)
* A brain (hard to find these days)

## Installation with Eclipse
* Download Eclipse Mars on http://www.eclipse.org - choose the modeling package
* Once you start, go in the Help menu and choose "Install New Software"
* Go to the install menu again and enter the following update site: https://raw.githubusercontent.com/cmu-sei/emfta/mars/edu.cmu.emfta.updatesite/


## Installation with OSATE
### Download the latest OSATE testing release 
Visit http://www.aadl.info/aadl/osate/testing/products/ and download
the last OSATE release for your architecture and OS. Basically,
it consists of a big zip file to unzip on your machine.

### Start OSATE and install EMFTA inside your installation
Once you start OSATE, go in the install menu again and enter the 
following update site: https://raw.githubusercontent.com/cmu-sei/emfta/master/edu.cmu.emfta.updatesite/

Select to install EMFTA and complete the installation.
![EMFTA Installation](https://raw.githubusercontent.com/cmu-sei/emfta/master/doc/pics/emfta-install.png "Installation of EMFTA")


## How to use

### Using the example wizard
Start eclipse and choose **File** and then **Others**. Then, select **EMFTA Wizards** and **EMFTA New File**.
You should have a window similar to the following then.
![Wizard Selection](https://raw.githubusercontent.com/cmu-sei/emfta/master/doc/pics/wizard-selection.png "Wizard Selection")

The wizard let you select the target project, the target file name as well as the template you want to import, as shown in the following picture.
![New File Wizard](https://raw.githubusercontent.com/cmu-sei/emfta/master/doc/pics/wizard-page2.png "New File Wizard")

Once completed, the new file is available in the target directory. Please note that you still need to add the **Modeling Nature** to the project that contains the file to enable the graphical and table representations.

### Getting Started
* Start a new modelling project (New->Modelling Project)
* Create a new EMFTA inside the modeling project with the root object FTA Model (New->Emfta Model)
* Make sure to be in the Sirius perspective, by selecting the Sirius perspective as shown below

![Sirius perspective](https://raw.githubusercontent.com/cmu-sei/emfta/master/doc/pics/sirius-perspective.png "Sirius Perspective")


* Select the EMFTA viewpoint for the project by making a right click on the project and select EMFTA as shown below

![Viewpoint Selection](https://raw.githubusercontent.com/cmu-sei/emfta/master/doc/pics/viewpoint-selection.png "Viewpoint Selection")

* Select the root object of the FTA model and choose the appropriate selection (Table or Tree) as shown below

![New Representation](https://raw.githubusercontent.com/cmu-sei/emfta/master/doc/pics/new-representation.png "New Representation")


### Cutset Generation
You can generate the cutset of an FTA model. It generates a CSV file (called cutset.csv) at the top of your project.
To generate the cutset, you have to select the top level error in the graphical view, right-click and select "Cutset"
as shown below. Once the cutset is generated, a new file (*cutset.csv*) is generated.

![Generating Cutset](https://raw.githubusercontent.com/cmu-sei/emfta/master/doc/pics/cutset.png "Generating Cutset")


## Examples

### The lamp example
The lamp example shows a simple FTA related to a lack of light
in a room. Basically, you have no light if the bulb is broken
or there is no more power. This example is a basic FTA model
that captures these two conditions with the logical operation (the AND).


![Diagram of the Lamp Example](https://github.com/cmu-sei/emfta/raw/master/example.emfta/imgs/example1-diagram.png "Diagram of the lamp example")


![Table of the Lamp Example](https://github.com/cmu-sei/emfta/raw/master/example.emfta/imgs/example1-table.png "Table for editing the Gates/Events of the lamp example")


### The computer example
The computer example represents a fault-tree representing
the conditions when a computer crashes. In this
example, we consider the following events to be included in the FTA:
* Unhandled interrupt - the processor crashes
* Device broken - the device sends continuously interrupts so that the processor is always busy
* A divide by zero exception is raised when excuting a piece of software
* A fault is raised in the exception handler

The following pictures show the tables (to edit the events/gates) as well as the diagram representation.

![Diagram of the Computer Example](https://github.com/cmu-sei/emfta/raw/master/example.emfta/imgs/example2-diagram.png "Diagram of the computer example")

![Table of the Computer Example](https://github.com/cmu-sei/emfta/raw/master/example.emfta/imgs/example2-table.png "Table for editing the Gates/Events of the computer example")


## Documentation
There is a documentation included into Eclipse. After installing EMFTA,
go into the Eclipse documentation, there is a dedicated section for EMFTA.

## Bugs and Features Requests
If you find a bug, please report it on the issue tracker of the project.
Also, if you have any suggestion/idea to improve the tool, please also add it
on the issue tracker! Please also read the following section about the known
limitations and future work on this project. Bug fixes and improvements
will be considered according to my free time and the number of miles I run per day.

## Special Thanks
To Bill Fletcher for his help and feedback! Much appreciated!

## Known Limitations and/or Future work
* Use for security examples (medium priority)
* Make Simulation (low priority)
