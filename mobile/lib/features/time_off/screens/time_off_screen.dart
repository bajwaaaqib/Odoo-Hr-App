import 'package:flutter/material.dart';

class TimeOffScreen extends StatelessWidget {
  const TimeOffScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final timeOffRecords = [
      {
        'type': 'Annual Leave',
        'status': 'Approved',
        'startDate': 'Sep 15, 2024',
        'endDate': 'Sep 20, 2024',
        'duration': '6 days',
      },
      {
        'type': 'Sick Leave',
        'status': 'Pending',
        'startDate': 'Oct 01, 2024',
        'endDate': 'Oct 02, 2024',
        'duration': '2 days',
      },
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Time Off'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Summary Card
            Card(
              color: Colors.blue.shade50,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    _buildLeaveSummary('Annual', '5 days', Colors.blue),
                    _buildLeaveSummary('Sick', '10 days', Colors.green),
                    _buildLeaveSummary('Emergency', '3 days', Colors.orange),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              'Your Requests',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            ...timeOffRecords.map((record) => Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          record['type']!,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 12,
                            vertical: 4,
                          ),
                          decoration: BoxDecoration(
                            color: record['status'] == 'Approved'
                                ? Colors.green.shade50
                                : Colors.orange.shade50,
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Text(
                            record['status']!,
                            style: TextStyle(
                              color: record['status'] == 'Approved'
                                  ? Colors.green.shade700
                                  : Colors.orange.shade700,
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        const Icon(Icons.calendar_today, size: 16, color: Colors.grey),
                        const SizedBox(width: 8),
                        Text('${record['startDate']} - ${record['endDate']}'),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        const Icon(Icons.access_time, size: 16, color: Colors.grey),
                        const SizedBox(width: 8),
                        Text(record['duration']!),
                      ],
                    ),
                  ],
                ),
              ),
            )),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          // TODO: Implement leave request
        },
        icon: const Icon(Icons.add),
        label: const Text('Request Leave'),
      ),
    );
  }

  Widget _buildLeaveSummary(String type, String days, Color color) {
    return Column(
      children: [
        CircleAvatar(
          backgroundColor: color,
          radius: 24,
          child: Text(
            type[0],
            style: const TextStyle(color: Colors.white, fontSize: 20),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          days,
          style: const TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 14,
          ),
        ),
        Text(
          type,
          style: const TextStyle(
            fontSize: 12,
            color: Colors.grey,
          ),
        ),
      ],
    );
  }
}
