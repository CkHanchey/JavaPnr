// Use this environment to target the .NET API on port 5000.
// Switch in angular.json by adding a "dotnet" build configuration.
export const environment = {
  production: false,
  apiUrl: 'http://localhost:5000/api',
  apiPaths: {
    edifact: 'Edifact',
    bulkEdifact: 'BulkEdifact',
    sampleData: 'SampleData',
    reservations: 'Reservations'
  }
};
