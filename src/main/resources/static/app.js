const API_BASE_URL = 'http://localhost:8080/api/strategy';

// fetches drivers and populate tracks when the page loads
document.addEventListener('DOMContentLoaded', function() {
    fetchDrivers();
    populateTracks();
});

// function to fetch all drivers
function fetchDrivers() {
    fetch('/api/drivers')
        .then(response => response.json())
        .then(drivers => {
            const driverSelect = document.getElementById('driverName');
            drivers.forEach(driver => {
                const option = document.createElement('option');
                option.value = driver.driverName;
                option.textContent = `${driver.driverName} (${driver.team})`;
                driverSelect.appendChild(option);
            });
        })
        .catch(error => console.error('Error fetching drivers:', error));
}

function populateTracks() {
    const trackSelect = document.getElementById('trackName');
    F1_2024_TRACKS.forEach(track => {
        const option = document.createElement('option');
        option.value = track.trackName;
        option.textContent = `${track.trackName} (${track.raceDate})`;
        trackSelect.appendChild(option);
    });
}

// handle driver selection
document.getElementById('driverName').addEventListener('change', function() {
    const selectedDriver = this.value;
    if (selectedDriver) {
        fetch('/api/drivers')
            .then(response => response.json())
            .then(drivers => {
                const driver = drivers.find(d => d.driverName === selectedDriver);
                if (driver) {
                    document.getElementById('team').value = driver.team;
                    document.getElementById('aggressionIndex').value = driver.aggressionIndex;
                    document.getElementById('overtakingAbility').value = driver.overtakingAbility;
                    document.getElementById('consistency').value = driver.consistency;
                }
            })
            .catch(error => console.error('Error fetching driver details:', error));
    } else {
        // clears fields if no driver is selected
        document.getElementById('team').value = '';
        document.getElementById('aggressionIndex').value = '';
        document.getElementById('overtakingAbility').value = '';
        document.getElementById('consistency').value = '';
    }
});

// handle track selection
document.getElementById('trackName').addEventListener('change', function() {
    const selectedTrack = this.value;
    if (selectedTrack) {
        const track = F1_2024_TRACKS.find(t => t.trackName === selectedTrack);
        if (track) {
            document.getElementById('trackTemperature').value = track.trackTemperature;
            document.getElementById('weatherCondition').value = track.weatherCondition;
            document.getElementById('trackLength').value = track.trackLength;
            document.getElementById('numberOfCorners').value = track.numberOfCorners;
            // sets default number of laps based on track length (approximately 305km race distance)
            const defaultLaps = Math.round(305 / track.trackLength);
            document.getElementById('numberOfLaps').value = defaultLaps;
        }
    } else {
        // clears fields if no track is selected
        document.getElementById('trackTemperature').value = '';
        document.getElementById('weatherCondition').value = 'DRY';
        document.getElementById('trackLength').value = '';
        document.getElementById('numberOfCorners').value = '';
        document.getElementById('numberOfLaps').value = '';
    }
});

function getFormData() {
    const formData = {
        trackName: document.getElementById('trackName').value,
        trackLength: parseFloat(document.getElementById('trackLength').value),
        numberOfLaps: parseInt(document.getElementById('numberOfLaps').value),
        numberOfCorners: parseInt(document.getElementById('numberOfCorners').value),
        trackTemperature: parseFloat(document.getElementById('trackTemperature').value),
        weatherCondition: document.getElementById('weatherCondition').value,
        driverName: document.getElementById('driverName').value,
        team: document.getElementById('team').value,
        aggressionIndex: parseFloat(document.getElementById('aggressionIndex').value),
        overtakingAbility: parseFloat(document.getElementById('overtakingAbility').value),
        consistency: parseFloat(document.getElementById('consistency').value)
    };
    console.log('Form data:', formData);
    return formData;
}

async function calculateStrategy() {
    try {
        const formData = getFormData();
        console.log('Sending calculate strategy request with data:', formData);
        
        const response = await fetch(`${API_BASE_URL}/calculate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            console.error('Server error response:', errorData);
            throw new Error(errorData.message || 'Failed to calculate strategy');
        }
        
        const data = await response.json();
        console.log('Received calculate strategy response:', data);
        displayStrategyResults(data);
    } catch (error) {
        console.error('Error in calculateStrategy:', error);
        const resultsDiv = document.getElementById('results');
        resultsDiv.innerHTML = `
            <div class="alert alert-danger" role="alert">
                Error: ${error.message}
            </div>
        `;
    }
}

async function compareStrategies() {
    try {
        const formData = getFormData();
        console.log('Sending compare strategies request with data:', formData);
        
        const response = await fetch(`${API_BASE_URL}/compare`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            console.error('Server error response:', errorData);
            throw new Error(errorData.message || 'Failed to compare strategies');
        }
        
        const data = await response.json();
        console.log('Received compare strategies response:', data);
        displayStrategyComparison(data);
    } catch (error) {
        console.error('Error in compareStrategies:', error);
        const resultsDiv = document.getElementById('results');
        resultsDiv.innerHTML = `
            <div class="alert alert-danger" role="alert">
                Error: ${error.message}
            </div>
        `;
    }
}

async function optimizePitStop() {
    try {
        const formData = getFormData();
        console.log('Sending optimize pit stop request with data:', formData);
        
        const response = await fetch(`${API_BASE_URL}/optimize-pit-stop`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            console.error('Server error response:', errorData);
            throw new Error(errorData.message || 'Failed to optimize pit stop');
        }
        
        const data = await response.json();
        console.log('Received optimize pit stop response:', data);
        displayPitStopOptimization(data);
    } catch (error) {
        console.error('Error in optimizePitStop:', error);
        const resultsDiv = document.getElementById('results');
        resultsDiv.innerHTML = `
            <div class="alert alert-danger" role="alert">
                Error: ${error.message}
            </div>
        `;
    }
}

function displayStrategyResults(data) {
    const resultsDiv = document.getElementById('results');
    
    // checks if data exists and has required properties
    if (!data || !data.baseDegradation || !data.optimalPitWindows || !data.actualLapTimes || !data.tireCompoundsUsed) {
        resultsDiv.innerHTML = `
            <div class="alert alert-danger" role="alert">
                Error: Invalid data received from server
            </div>
        `;
        return;
    }

    // logs the data for debugging
    console.log('Strategy Results Data:', {
        actualLapTimes: data.actualLapTimes,
        tireCompoundsUsed: data.tireCompoundsUsed,
        optimalPitWindows: data.optimalPitWindows
    });

    resultsDiv.innerHTML = `
        <div class="card strategy-card">
            <div class="card-body">
                <h5 class="card-title">Optimal Strategy</h5>
                <p class="card-text">
                    <strong>Base Degradation:</strong> ${(data.baseDegradation * 100).toFixed(2)}%<br>
                    <strong>Optimal Pit Windows:</strong> ${data.optimalPitWindows.join(', ')}<br>
                    <strong>Tire Compounds:</strong> ${data.tireCompounds.join(' → ')}
                </p>
                <div class="chart-container">
                    <canvas id="lapTimeChart"></canvas>
                </div>
            </div>
        </div>
    `;

    // creates lap time chart
    const ctx = document.getElementById('lapTimeChart').getContext('2d');
    
    // prepares datasets based on tire compounds used
    const datasets = [];
    let currentCompound = data.tireCompoundsUsed[0];
    let currentData = [];
    let currentLabels = [];
    
    for (let i = 0; i < data.tireCompoundsUsed.length; i++) {
        if (data.tireCompoundsUsed[i] !== currentCompound) {
            // adds the previous dataset
            datasets.push({
                label: `${currentCompound} Tires`,
                data: currentData.map((value, index) => ({
                    x: currentLabels[index],
                    y: value
                })),
                borderColor: currentCompound === 'MEDIUM' ? '#00d2be' : 
                           currentCompound === 'SOFT' ? '#e10600' : '#1e1e1e',
                backgroundColor: currentCompound === 'MEDIUM' ? '#00d2be' : 
                              currentCompound === 'SOFT' ? '#e10600' : '#1e1e1e',
                tension: 0.1,
                fill: false,
                pointRadius: 3,
                pointHoverRadius: 5
            });
            
            // starts new dataset
            currentCompound = data.tireCompoundsUsed[i];
            currentData = [];
            currentLabels = [];
        }
        currentData.push(data.actualLapTimes[i]);
        currentLabels.push(i + 1);
    }
    
    // adds the last dataset
    datasets.push({
        label: `${currentCompound} Tires`,
        data: currentData.map((value, index) => ({
            x: currentLabels[index],
            y: value
        })),
        borderColor: currentCompound === 'MEDIUM' ? '#00d2be' : 
                   currentCompound === 'SOFT' ? '#e10600' : '#1e1e1e',
        backgroundColor: currentCompound === 'MEDIUM' ? '#00d2be' : 
                       currentCompound === 'SOFT' ? '#e10600' : '#1e1e1e',
        tension: 0.1,
        fill: false,
        pointRadius: 3,
        pointHoverRadius: 5
    });

    new Chart(ctx, {
        type: 'line',
        data: {
            datasets: datasets
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: false,
                    title: {
                        display: true,
                        text: 'Lap Time (seconds)'
                    }
                },
                x: {
                    type: 'linear',
                    position: 'bottom',
                    title: {
                        display: true,
                        text: 'Lap Number'
                    },
                    min: 1,
                    max: data.actualLapTimes.length,
                    ticks: {
                        stepSize: 1
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.dataset.label}: ${context.parsed.y.toFixed(3)}s`;
                        }
                    }
                }
            }
        }
    });
}

function displayStrategyComparison(data) {
    const resultsDiv = document.getElementById('results');
    
    // checks if data exists and has required properties
    if (!data || !data.strategies || !Array.isArray(data.strategies)) {
        resultsDiv.innerHTML = `
            <div class="alert alert-danger" role="alert">
                Error: Invalid data received from server
            </div>
        `;
        return;
    }

    resultsDiv.innerHTML = `
        <div class="row">
            ${data.strategies.map((strategy, index) => `
                <div class="col-md-6">
                    <div class="card strategy-card">
                        <div class="card-body">
                            <h5 class="card-title">${strategy.strategyType}</h5>
                            <p class="card-text">
                                <strong>Total Race Time:</strong> ${strategy.totalRaceTime.toFixed(2)} seconds<br>
                                <strong>Pit Stops:</strong> ${strategy.strategyType === 'ONE_STOP' ? '1' : '2'}<br>
                                <strong>Tire Compounds:</strong> ${strategy.tireCompounds.join(' → ')}<br>
                                ${strategy.strategyType === 'ONE_STOP' ? 
                                    `<strong>Pit Stop Lap:</strong> ${strategy.pitLaps[0]}` : 
                                    `<strong>First Pit Stop:</strong> ${strategy.pitLaps[0]}<br>
                                     <strong>Second Pit Stop:</strong> ${strategy.pitLaps[1]}`
                                }
                            </p>
                        </div>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}

function displayPitStopOptimization(data) {
    const resultsDiv = document.getElementById('results');
    
    // checks if data exists and has required properties
    if (!data || !data.strategyType || !data.totalRaceTime || !data.optimalPitLap || !data.actualLapTimes || !data.tireCompoundsUsed) {
        resultsDiv.innerHTML = `
            <div class="alert alert-danger" role="alert">
                Error: Invalid data received from server
            </div>
        `;
        return;
    }

    // logs the data for debugging
    console.log('Pit Stop Optimization Data:', data);
    console.log('Pit Stop Analysis Data:', data.pitStopAnalysis);

    // creates main strategy card
    let html = `
        <div class="card strategy-card">
            <div class="card-body">
                <h5 class="card-title">Optimized Pit Stop Strategy</h5>
                <p class="card-text">
                    <strong>Strategy Type:</strong> ${data.strategyType}<br>
                    <strong>Optimal Pit Stop Lap:</strong> ${data.optimalPitLap}<br>
                    <strong>Total Race Time:</strong> ${data.totalRaceTime.toFixed(2)} seconds<br>
                    <strong>Tire Compounds:</strong> ${data.tireCompounds.join(' → ')}<br>
                    <strong>Pit Stop Time Loss:</strong> ${data.pitStopTimeLoss.toFixed(2)} seconds<br>
                    <strong>Estimated Position Impact:</strong> ${data.positionImpact.toFixed(1)} positions<br>
                    <strong>Tire Degradation at Pit Stop:</strong> ${data.tireDegradationAtPit.toFixed(1)}%
                </p>
                <div class="chart-container">
                    <canvas id="optimizationChart"></canvas>
                </div>
            </div>
        </div>
    `;

    // adds pit stop analysis table
    html += `
        <div class="card mt-4">
            <div class="card-header">
                <h5>Pit Stop Window Analysis</h5>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>Pit Lap</th>
                                <th>Total Time</th>
                                <th>Time Difference</th>
                                <th>Position Impact</th>
                                <th>Tire Degradation</th>
                            </tr>
                        </thead>
                        <tbody>
    `;

    // sorts pit stop analysis by pit lap
    const sortedAnalysis = [...data.pitStopAnalysis].sort((a, b) => a.pitLap - b.pitLap);
    
    // finds the index of the optimal pit stop
    const optimalIndex = sortedAnalysis.findIndex(analysis => analysis.pitLap === data.optimalPitLap);
    
    // gets the 5 laps after the optimal pit stop
    const startIndex = Math.max(0, optimalIndex - 5);
    const endIndex = Math.min(sortedAnalysis.length, optimalIndex + 6);
    const displayAnalysis = sortedAnalysis.slice(startIndex, endIndex);
    
    console.log('Display Analysis:', displayAnalysis);
    
    // adds rows for each pit stop window
    displayAnalysis.forEach(analysis => {
        const isOptimal = analysis.pitLap === data.optimalPitLap;
        const timeDiff = analysis.timeDifference;
        const timeDiffClass = timeDiff > 0 ? 'text-danger' : 'text-success';
        const positionImpact = analysis.positionImpact;
        const positionClass = positionImpact > 0 ? 'text-danger' : 'text-success';
        
        html += `
            <tr ${isOptimal ? 'class="table-success"' : ''}>
                <td>${analysis.pitLap}</td>
                <td>${analysis.totalRaceTime.toFixed(2)}s</td>
                <td class="${timeDiffClass}">${timeDiff.toFixed(2)}s</td>
                <td class="${positionClass}">${positionImpact.toFixed(1)}</td>
                <td>${analysis.tireDegradationAtPit.toFixed(1)}%</td>
            </tr>
        `;
    });

    html += `
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `;

    resultsDiv.innerHTML = html;

    // creates optimization chart
    const ctx = document.getElementById('optimizationChart').getContext('2d');
    
    // creates a single dataset with segment colors based on tire compounds
    const dataset = {
        label: 'Lap Times',
        data: data.actualLapTimes,
        borderColor: data.tireCompoundsUsed.map(compound => getTireColor(compound)),
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        fill: false,
        tension: 0.1,
        segment: {
            borderColor: ctx => getTireColor(data.tireCompoundsUsed[ctx.p0DataIndex])
        }
    };

    // creates the chart
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: Array.from({length: data.actualLapTimes.length}, (_, i) => i + 1),
            datasets: [dataset]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Lap Times by Tire Compound'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const lapNumber = context.dataIndex + 1;
                            const compound = data.tireCompoundsUsed[context.dataIndex];
                            return `Lap ${lapNumber} (${compound}): ${context.raw.toFixed(3)}s`;
                        }
                    }
                },
                legend: {
                    display: true,
                    labels: {
                        generateLabels: function(chart) {
                            const compounds = [...new Set(data.tireCompoundsUsed)];
                            return compounds.map(compound => ({
                                text: compound,
                                fillStyle: getTireColor(compound),
                                strokeStyle: getTireColor(compound),
                                lineWidth: 2
                            }));
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    title: {
                        display: true,
                        text: 'Lap Time (seconds)'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Lap Number'
                    }
                }
            }
        }
    });
}

function getTireColor(compound) {
    switch (compound) {
        case 'SOFT': return '#e10600';    // F1 Red for Soft
        case 'MEDIUM': return '#00d2be';  // F1 Teal for Medium
        case 'HARD': return '#1e1e1e';    // F1 Black for Hard
        default: return 'rgb(128, 128, 128)';
    }
}