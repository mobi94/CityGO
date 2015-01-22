//
//  CGMainViewController.m
//  CityGo
//
//  Created by ruslan on 1/22/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGMainViewController.h"

@interface CGMainViewController ()

@property(strong, nonatomic) id observer;

- (void)registerObserver;
- (void)unregisterObserver;

@end

@implementation CGMainViewController

#pragma mark -
#pragma mark Lifecycle

- (void)awakeFromNib
{
    [super awakeFromNib];
    
    [self registerObserver];
}

- (void)dealloc
{
    [self unregisterObserver];
}

#pragma mark -
#pragma mark Observer

- (void)registerObserver
{
    typeof(self) __weak weakSelf = self;
    
    [self setObserver:[NOTIFICATION_CENTER addObserverForName:@"DismissModal"
                                                       object:nil
                                                        queue:MAIN_QUEUE
                                                   usingBlock:^(NSNotification *note)
                       {
                           [weakSelf dismissViewControllerAnimated:YES
                                                        completion:nil];
                       }]];
}

- (void)unregisterObserver
{
    [NOTIFICATION_CENTER removeObserver:_observer];
}


@end
