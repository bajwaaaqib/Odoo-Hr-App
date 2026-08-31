import 'package:flutter/material.dart';

class AttendanceHistoryScreen extends StatelessWidget {
  const AttendanceHistoryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final attendanceRecords = [
      {'date': '29 Aug', 'checkIn': '08:42', 'checkOut': '17:31', 'duration': '8h 49m'},
      {'date': '28 Aug', 'checkIn': '08:37', 'checkOut': '17:22', 'duration': '8h 45m'},
      {'date': '27 Aug', 'checkIn': '08:51', 'checkOut': '17:40', 'duration': '8h 49m'},
      {'date': '26 Aug', 'checkIn': '08:45', 'checkOut': '17:35', 'duration': '8h 50m'},
      {'date': '25 Aug', 'checkIn': '08:30', 'checkOut': '17:20', 'duration': '8h 50m'},
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Attendance History'),
      ),
      body: ListView.builder(
        padding: const EdgeInsets.all(16.0),
        itemCount: attendanceRecords.length,
        itemBuilder: (context, index) {
          final record = attendanceRecords[index];
          return Card(
            child: ListTile(
              leading: CircleAvatar(
                backgroundColor: Theme.of(context).colorScheme.primary,
                child: Text(
                  record['date']!.split(' ')[0],
                  style: const TextStyle(color: Colors.white, fontSize: 12),
                ),
              ),
              title: Text(record['date']!),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      const Icon(Icons.login, size: 16),
                      const SizedBox(width: 4),
                      Text(record['checkIn']!),
                      const SizedBox(width: 16),
                      const Icon(Icons.logout, size: 16),
                      const SizedBox(width: 4),
                      Text(record['checkOut']!),
                    ],
                  ),
                ],
              ),
              trailing: Text(
                record['duration']!,
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                  color: Colors.green,
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}
