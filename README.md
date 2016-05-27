# MIKELANGELO Applications for OSv

To use the repository, you have to clone it first on your machine.

Then, you must edit the ```repositories``` section of the ```$OSV_HOME/config.json``` file, for example

```bash
    "repositories": [
        "${OSV_BASE}/apps",
        "${OSV_BASE}/modules",
        "/path/to/mike/apps"
    ]
```

Make sure that existing repositories are preserved in case you would like to use applications and modules from those repositories. 

To build OSv images integrating applications from the Mikelangelo apps repository use

```bash
make image=OpenFOAM
```

You can still include other modules and applications. The following will build an OSv image and include the OpenFOAM application and the HTTP REST server providing OSv API layer.

```bash
make image=OpenFOAM,httpserver
```
