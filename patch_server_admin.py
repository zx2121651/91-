import re

file_path = "Aurelian-Backend/server.js"
with open(file_path, "r") as f:
    content = f.read()

# Add admin route import
if "const adminRoutes = require('./src/routes/admin');" not in content:
    content = content.replace(
        "const hookupsRoutes = require('./src/routes/hookups');",
        "const hookupsRoutes = require('./src/routes/hookups');\nconst adminRoutes = require('./src/routes/admin');"
    )

# Mount the admin route
if "app.use('/api/v1/admin', adminRoutes);" not in content:
    content = content.replace(
        "app.use('/api/v1/hookups', hookupsRoutes);",
        "app.use('/api/v1/hookups', hookupsRoutes);\napp.use('/api/v1/admin', adminRoutes);"
    )

with open(file_path, "w") as f:
    f.write(content)
