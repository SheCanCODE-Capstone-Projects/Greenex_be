//package org.example.greenexproject.Utils;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//import jakarta.annotation.PostConstruct;
//
//@Component
//@RequiredArgsConstructor
//public class FixNotificationConstraint {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    @PostConstruct
//    public void fixConstraint() {
//        try {
//            // DROP old constraint (exact lowercase table + constraint)
//            jdbcTemplate.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;");
//
//            // ADD new constraint including all enum values
//            jdbcTemplate.execute("""
//                ALTER TABLE notifications
//                ADD CONSTRAINT notifications_type_check
//                CHECK (type IN (
//                    'COMPANY_REGISTERED',
//                    'COMPANY_APPROVED',
//                    'COMPANY_REJECTED',
//                    'NEW_COMPLAINT',
//                    'COMPLAINT_RESOLVED',
//                    'COMPLAINT_UPDATED',
//                    'PAYMENT_SUCCESS',
//                    'PAYMENT_REMINDER',
//                    'BILL_GENERATED',
//                    'PICKUP_COMPLETED',
//                    'PICKUP_SKIPPED',
//                    'ROUTE_ASSIGNED',
//                    'SESSION_ASSIGNED',
//                    'PAYMENT',
//                    'COMPLAINT',
//                    'BILLING',
//                    'PROMOTIONAL',
//                    'GENERAL'
//                ));
//            """);
//
//            System.out.println(" Notification type constraint updated successfully!");
//        } catch (Exception e) {
//            System.err.println(" Failed to update constraint: " + e.getMessage());
//        }
//    }
//}
