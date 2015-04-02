# This is the home of all applications used within Mikelangelo project.

To use the repository, you have to clone it first. Then, you must edit the ```repositories``` section of the ```$OSV_HOME/config.json``` file, for example

```bash
    "repositories": [
        "${OSV_BASE}/apps",
        "${OSV_BASE}/modules",
        "/path/to/mike/apps"
    ]
```

Now you can simply build OSv images integrating applications from the Mikelangelo apps repository

```bash
make image=OpenFOAM
```

You can still include other modules and applications. The following will build an OSv image and include the OpenFOAM application and the HTTP REST server providing OSv API layer.

```bash
make image=OpenFOAM,httpserver
```
