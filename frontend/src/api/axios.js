import axios from "axios";  
// Create an instance of axios with a custom configuration
export default axios.create({
// Set the base URL for all requests made using this instance
    baseURL: "http://localhost:8080/api",
})

