//
//  CGNotifier.m
//  CityGo
//
//  Created by ruslan on 1/22/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGNotifier.h"

@implementation CGNotifier

#pragma mark -
#pragma mark Register observer
    
+ (void)postDismissModalNotification
{
    [NOTIFICATION_CENTER postNotificationName:@"DismissModal" object:nil];
}

@end
