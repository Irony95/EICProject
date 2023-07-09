const express = require('express')
const app = express()
const port = 80

app.get("/", (req, res) => {
    res.send("1.29")
})

app.listen(port, () => {
    console.log(`Server started on port ${port}`)
})